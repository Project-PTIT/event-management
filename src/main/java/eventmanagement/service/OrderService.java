package eventmanagement.service;

import eventmanagement.dto.*;
import eventmanagement.entity.*;
import eventmanagement.exception.BadRequestException;
import eventmanagement.exception.ForbiddenException;
import eventmanagement.exception.ResourceNotFoundException;
import eventmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;

    /**
     * Lấy ID khách hàng hiện tại (mặc định KH001 theo tài liệu giai đoạn dev)
     */
    private String resolveCurrentCustomerId() {
        return "KH001";
    }

    /**
     * Tự động sinh mã đơn hàng dạng DH001, DH002... chuẩn theo tài liệu
     */
    private synchronized String generateNewOrderId() {
        long count = orderRepository.count() + 1;
        return String.format("DH%03d", count);
    }

    /**
     * 1. Tạo đơn đặt vé mới (status mặc định: 'Chờ thanh toán')
     */
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        String customerId = resolveCurrentCustomerId();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khách hàng: " + customerId));

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Danh sách vé đặt không được để trống");
        }

        // Gộp các ticketId trùng lặp trong request
        Map<String, Integer> aggregatedItems = new HashMap<>();
        for (OrderItemRequest item : request.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new BadRequestException("Số lượng vé phải lớn hơn 0");
            }
            aggregatedItems.merge(item.getTicketId(), item.getQuantity(), Integer::sum);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Ticket> tickets = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : aggregatedItems.entrySet()) {
            String ticketId = entry.getKey();
            int qty = entry.getValue();

            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé: " + ticketId));

            // Kiểm tra trạng thái suất diễn
            EventSchedule schedule = ticket.getSchedule();
            if (schedule == null) {
                throw new BadRequestException("Vé không thuộc suất diễn hợp lệ");
            }

            if (schedule.getEndDatetime() != null && schedule.getEndDatetime().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Suất diễn đã kết thúc, không thể đặt vé");
            }

            // Trừ vé trực tiếp bằng DB
            int updated = ticketRepository.decreaseRemainingQuantity(ticketId, qty);
            if (updated == 0) {
                throw new BadRequestException("Vé '" + ticket.getTicketName() + "' đã hết hoặc không đủ số lượng yêu cầu");
            }

            totalAmount = totalAmount.add(ticket.getPrice().multiply(BigDecimal.valueOf(qty)));
            tickets.add(ticket);
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Tổng tiền đơn hàng phải lớn hơn 0");
        }

        // Tạo Order
        String orderId = generateNewOrderId();
        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomer(customer);
        order.setTotalAmount(totalAmount);
        order.setStatus("Chờ thanh toán");
        order.setCreatedAt(LocalDateTime.now());
        order.setPaymentAt(null);
        order.setPaymentMethod(null);

        Order savedOrder = orderRepository.save(order);

        // Lưu OrderDetail
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (Ticket ticket : tickets) {
            int qty = aggregatedItems.get(ticket.getTicketId());
            OrderDetail detail = new OrderDetail();
            
            detail.setId(new OrderDetailId(savedOrder.getOrderId(), ticket.getTicketId()));
            detail.setOrder(savedOrder);
            detail.setTicket(ticket);
            detail.setQuantity(qty);
            detail.setPrice(ticket.getPrice());

            orderDetails.add(orderDetailRepository.save(detail));
        }

        return mapToOrderResponse(savedOrder, orderDetails);
    }

    /**
     * 2. Lấy lịch sử đơn hàng của người dùng hiện tại
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String status) {
        String customerId = resolveCurrentCustomerId();
        List<Order> orders;

        if (status != null && !status.trim().isEmpty()) {
            orders = orderRepository.findByCustomer_IdAndStatusOrderByCreatedAtDesc(customerId, status.trim());
        } else {
            orders = orderRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId);
        }

        return orders.stream()
                .map(order -> {
                    List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(order.getOrderId());
                    return mapToOrderResponse(order, details);
                })
                .collect(Collectors.toList());
    }

    /**
     * 3. Xem chi tiết đơn hàng
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(String orderId) {
        String customerId = resolveCurrentCustomerId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("Bạn không có quyền truy cập vào đơn hàng này");
        }

        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(order.getOrderId());
        return mapToOrderResponse(order, details);
    }

    /**
     * 4. Thanh toán đơn hàng
     */
    @Transactional
    public OrderResponse payOrder(String orderId, PayOrderRequest request) {
        String customerId = resolveCurrentCustomerId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("Bạn không có quyền thanh toán đơn hàng này");
        }

        if (!"Chờ thanh toán".equals(order.getStatus())) {
            throw new BadRequestException("Đơn hàng không ở trạng thái 'Chờ thanh toán'");
        }

        order.setStatus("Đã thanh toán");
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(orderId);
        return mapToOrderResponse(updatedOrder, details);
    }

    /**
     * 5. Hủy đơn hàng và hoàn lại vé
     */
    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        String customerId = resolveCurrentCustomerId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("Bạn không có quyền hủy đơn hàng này");
        }

        if (!"Chờ thanh toán".equals(order.getStatus())) {
            throw new BadRequestException("Chỉ có thể hủy đơn hàng đang ở trạng thái 'Chờ thanh toán'");
        }

        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(orderId);
        for (OrderDetail detail : details) {
            ticketRepository.increaseRemainingQuantity(detail.getTicket().getTicketId(), detail.getQuantity());
        }

        order.setStatus("Đã hủy");
        order.setPaymentAt(null);

        Order cancelledOrder = orderRepository.save(order);
        return mapToOrderResponse(cancelledOrder, details);
    }

    private OrderResponse mapToOrderResponse(Order order, List<OrderDetail> details) {
        List<OrderDetailResponse> detailResponses = details.stream().map(d -> {
            OrderDetailResponse res = new OrderDetailResponse();
            res.setTicketId(d.getTicket().getTicketId());
            res.setTicketName(d.getTicket().getTicketName());
            res.setQuantity(d.getQuantity());
            res.setPrice(d.getPrice());
            res.setSubTotal(d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
            return res;
        }).collect(Collectors.toList());

        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setCustomerId(order.getCustomer().getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setCreatedAt(order.getCreatedAt());
        response.setPaymentAt(order.getPaymentAt());
        response.setDetails(detailResponses);

        return response;
    }
}