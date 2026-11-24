package eventmanagement.service;

import eventmanagement.dto.OrderCreateRequest;
import eventmanagement.dto.OrderDetailResponse;
import eventmanagement.dto.OrderItemRequest;
import eventmanagement.dto.OrderResponse;
import eventmanagement.dto.PayOrderRequest;
import eventmanagement.entity.Customer;
import eventmanagement.entity.Event;
import eventmanagement.entity.EventSchedule;
import eventmanagement.entity.Order;
import eventmanagement.entity.OrderDetail;
import eventmanagement.entity.Ticket;
import eventmanagement.exception.BadRequestException;
import eventmanagement.exception.ConflictException;
import eventmanagement.exception.ForbiddenException;
import eventmanagement.exception.ResourceNotFoundException;
import eventmanagement.repository.CustomerRepository;
import eventmanagement.repository.OrderDetailRepository;
import eventmanagement.repository.OrderRepository;
import eventmanagement.repository.TicketRepository;
import eventmanagement.util.AppTime;
import eventmanagement.util.CurrentUser;
import eventmanagement.util.IdGenerator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Nghiệp vụ đặt vé, thanh toán và hủy đơn của khách hàng.
 * Mọi thao tác ghi đều nằm trong một transaction; trừ/hoàn vé và đổi trạng thái đơn dùng
 * câu UPDATE có điều kiện nên an toàn khi nhiều request chạy đồng thời.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    // Các trạng thái đơn hàng, khớp CHECK của bảng order trong mydb.sql
    private static final String STATUS_PENDING = "Chờ thanh toán";
    private static final String STATUS_PAID = "Đã thanh toán";
    private static final String STATUS_CANCELLED = "Đã hủy";

    // Phương thức thanh toán hợp lệ (khớp các lựa chọn ở trang checkout.html)
    private static final Set<String> PAYMENT_METHODS = Set.of("Chuyển khoản", "Thẻ", "Ví điện tử");

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final EntityManager entityManager;

    // ======================= ĐẶT VÉ =======================

    /**
     * Tạo đơn mới với trạng thái 'Chờ thanh toán' và trừ vé.
     * Toàn bộ (tạo đơn, tạo chi tiết, trừ vé) nằm trong một transaction: bước nào lỗi thì rollback hết.
     */
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        String customerId = currentCustomerId();

        // 1. Kiểm tra request
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Danh sách vé đặt không được để trống.");
        }

        // 2. Gộp các ticketId trùng nhau. TreeMap giúp luôn xử lý vé theo cùng một thứ tự,
        //    tránh deadlock khi hai đơn đặt cùng các vé theo thứ tự khác nhau.
        Map<String, Integer> aggregated = new TreeMap<>();
        for (OrderItemRequest item : request.getItems()) {
            if (item == null || item.getTicketId() == null || item.getTicketId().isBlank()) {
                throw new BadRequestException("Mã vé không được để trống.");
            }
            if (item.getQuantity() <= 0) {
                throw new BadRequestException("Số lượng vé phải lớn hơn 0.");
            }
            try {
                aggregated.merge(item.getTicketId().trim(), item.getQuantity(), (a, b) -> Math.addExact(a, b));
            } catch (ArithmeticException overflow) {
                throw new BadRequestException("Số lượng vé quá lớn.");
            }
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khách hàng."));

        // 3. Kiểm tra từng vé và tính tổng tiền từ GIÁ TRONG DATABASE (không tin giá từ frontend)
        LocalDateTime now = AppTime.now();
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<String, Ticket> tickets = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : aggregated.entrySet()) {
            Ticket ticket = ticketRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé: " + entry.getKey()));

            EventSchedule schedule = ticket.getSchedule();
            Event event = schedule.getEvent();
            if (!"APPROVED".equals(event.getStatus())) {
                throw new BadRequestException("Sự kiện chưa được duyệt, không thể đặt vé.");
            }
            if (!schedule.getStartDatetime().isAfter(now)) {
                throw new BadRequestException("Suất diễn đã bắt đầu hoặc đã kết thúc, không thể đặt vé.");
            }

            totalAmount = totalAmount.add(ticket.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            tickets.put(ticket.getTicketId(), ticket);
        }
        // CHECK (total_amount > 0) của bảng order
        if (totalAmount.signum() <= 0) {
            throw new BadRequestException("Tổng tiền đơn hàng phải lớn hơn 0.");
        }

        // 4. Trừ vé bằng UPDATE có điều kiện. Không đủ vé thì ném lỗi và rollback toàn bộ.
        for (Map.Entry<String, Integer> entry : aggregated.entrySet()) {
            int updated = ticketRepository.decreaseRemainingQuantity(entry.getKey(), entry.getValue());
            if (updated == 0) {
                throw new BadRequestException("Vé '" + tickets.get(entry.getKey()).getTicketName()
                        + "' đã hết hoặc không đủ số lượng yêu cầu.");
            }
        }

        // 5. Tạo đơn + chi tiết. Mã đơn sinh bằng IdGenerator (DH001, DH002...)
        Order order = new Order();
        order.setOrderId(IdGenerator.nextId("DH", 3));
        order.setCustomer(customer);
        order.setTotalAmount(totalAmount);
        order.setStatus(STATUS_PENDING);
        order.setPaymentMethod(null);
        order.setPaymentAt(null);

        for (Map.Entry<String, Integer> entry : aggregated.entrySet()) {
            Ticket ticket = tickets.get(entry.getKey());
            OrderDetail detail = new OrderDetail();
            detail.setTicket(ticket);
            detail.setQuantity(entry.getValue());
            detail.setPrice(ticket.getPrice()); // lưu GIÁ TẠI THỜI ĐIỂM ĐẶT
            order.addDetail(detail);            // addDetail gắn order và khóa chính ghép cho chi tiết
        }

        entityManager.persist(order);  // cascade lưu luôn các chi tiết
        entityManager.flush();
        entityManager.refresh(order);  // nạp created_at do DB tự điền (cột insertable = false)

        return toResponse(order, order.getDetails());
    }

    // ======================= XEM ĐƠN =======================

    /** Lịch sử đơn của khách đang đăng nhập, có thể lọc theo trạng thái. */
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String status) {
        String customerId = currentCustomerId();
        List<Order> orders = (status != null && !status.isBlank())
                ? orderRepository.findByCustomer_IdAndStatusOrderByCreatedAtDesc(customerId, status.trim())
                : orderRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId);

        List<OrderResponse> result = new ArrayList<>();
        for (Order order : orders) {
            result.add(toResponse(order, orderDetailRepository.findByOrder_OrderId(order.getOrderId())));
        }
        return result;
    }

    /** Chi tiết một đơn; chỉ chủ đơn mới xem được. */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(String orderId) {
        Order order = findOwnedOrder(orderId, currentCustomerId());
        return toResponse(order, orderDetailRepository.findByOrder_OrderId(orderId));
    }

    // ======================= THANH TOÁN =======================

    /** Thanh toán đơn 'Chờ thanh toán'. Đơn đã thanh toán hoặc đã hủy không thanh toán lại được. */
    @Transactional
    public OrderResponse payOrder(String orderId, PayOrderRequest request) {
        String customerId = currentCustomerId();
        findOwnedOrder(orderId, customerId); // 404 nếu không có đơn, 403 nếu không phải của mình

        String method = (request == null || request.getPaymentMethod() == null)
                ? "" : request.getPaymentMethod().trim();
        if (!PAYMENT_METHODS.contains(method)) {
            throw new BadRequestException("Phương thức thanh toán không hợp lệ. Chọn một trong: "
                    + String.join(", ", PAYMENT_METHODS) + ".");
        }

        // Một câu UPDATE có điều kiện status = 'Chờ thanh toán': an toàn khi bấm thanh toán hai lần cùng lúc
        int updated = orderRepository.markPaid(orderId, customerId, method);
        if (updated == 0) {
            throw new BadRequestException("Chỉ thanh toán được đơn đang ở trạng thái 'Chờ thanh toán'.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));
        return toResponse(order, orderDetailRepository.findByOrder_OrderId(orderId));
    }

    // ======================= HỦY ĐƠN =======================

    /**
     * Hủy đơn 'Chờ thanh toán' và hoàn vé đúng một lần.
     * Nếu bất kỳ bước hoàn vé nào thất bại, cả transaction (kể cả việc đổi trạng thái đơn) bị rollback.
     */
    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        String customerId = currentCustomerId();
        findOwnedOrder(orderId, customerId);

        // Bước 1: đổi trạng thái có điều kiện. Request hủy thứ hai (chạy đồng thời) sẽ nhận 0 ở đây
        // nên không bao giờ hoàn vé lần thứ hai.
        int cancelled = orderRepository.markCancelled(orderId, customerId);
        if (cancelled == 0) {
            throw new BadRequestException("Chỉ hủy được đơn đang ở trạng thái 'Chờ thanh toán'.");
        }

        // Bước 2: hoàn vé; UPDATE có điều kiện remaining + qty <= total nên không vượt tổng số vé
        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(orderId);
        for (OrderDetail detail : details) {
            int restored = ticketRepository.increaseRemainingQuantity(
                    detail.getTicket().getTicketId(), detail.getQuantity());
            if (restored == 0) {
                // Dữ liệu vé không nhất quán: ném lỗi để rollback, không hoàn dở dang
                throw new ConflictException("Không thể hoàn vé cho đơn " + orderId
                        + " vì số liệu vé không nhất quán. Thao tác đã được hoàn tác.");
            }
        }

        // markCancelled đã làm sạch persistence context nên nạp lại đơn và chi tiết (đang gắn session) để dựng response
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));
        return toResponse(order, orderDetailRepository.findByOrder_OrderId(orderId));
    }

    // ======================= YÊU CẦU HOÀN TIỀN =======================

    /** Khách chỉ được gửi yêu cầu hoàn tiền khi còn ít nhất chừng này giờ trước suất diễn sớm nhất của đơn. */
    public static final int REFUND_MIN_HOURS_BEFORE_EVENT = 24;

    /** Khách gửi yêu cầu hoàn tiền cho đơn đã thanh toán; Admin sẽ duyệt hoặc từ chối. */
    @Transactional
    public OrderResponse requestRefund(String orderId, String reason) {
        String customerId = currentCustomerId();
        findOwnedOrder(orderId, customerId);

        String text = reason == null ? "" : reason.trim();
        if (text.length() < 5 || text.length() > 500) {
            throw new BadRequestException("Lý do hoàn tiền phải từ 5 đến 500 ký tự.");
        }

        LocalDateTime deadline = refundDeadline(orderDetailRepository.findByOrder_OrderId(orderId));
        if (deadline != null && !AppTime.now().isBefore(deadline)) {
            throw new BadRequestException("Đã quá hạn yêu cầu hoàn tiền: phải gửi trước giờ diễn ít nhất "
                    + REFUND_MIN_HOURS_BEFORE_EVENT + " giờ.");
        }

        // UPDATE có điều kiện: chỉ 'Đã thanh toán' và đúng chủ đơn; gửi hai lần liên tiếp chỉ thành công một lần
        if (orderRepository.markRefundRequested(orderId, customerId, text) == 0) {
            throw new BadRequestException("Chỉ gửi được yêu cầu hoàn tiền cho đơn đang ở trạng thái 'Đã thanh toán'.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));
        return toResponse(order, orderDetailRepository.findByOrder_OrderId(orderId));
    }

    /** Khách rút lại yêu cầu hoàn tiền khi Admin chưa xử lý. */
    @Transactional
    public OrderResponse withdrawRefundRequest(String orderId) {
        String customerId = currentCustomerId();
        findOwnedOrder(orderId, customerId);
        if (orderRepository.markRefundRequestWithdrawn(orderId, customerId) == 0) {
            throw new BadRequestException("Đơn không có yêu cầu hoàn tiền nào đang chờ xử lý.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));
        return toResponse(order, orderDetailRepository.findByOrder_OrderId(orderId));
    }

    /** Hạn chót gửi yêu cầu hoàn tiền = suất diễn sớm nhất trong đơn trừ 24 giờ. null nếu đơn không có chi tiết. */
    private LocalDateTime refundDeadline(List<OrderDetail> details) {
        LocalDateTime earliest = null;
        for (OrderDetail d : details) {
            LocalDateTime start = d.getTicket().getSchedule().getStartDatetime();
            if (earliest == null || start.isBefore(earliest)) {
                earliest = start;
            }
        }
        return earliest == null ? null : earliest.minusHours(REFUND_MIN_HOURS_BEFORE_EVENT);
    }

    // ======================= HỦY ĐƠN QUÁ HẠN =======================

    /** Thời gian giữ chỗ tối đa cho đơn 'Chờ thanh toán'. */
    public static final int PENDING_TIMEOUT_MINUTES = 15;

    @Transactional
    public void cancelExpiredOne(String orderId) {
        if (orderRepository.markExpiredCancelled(orderId) == 0) {
            return; // đã được thanh toán/hủy trong lúc đó
        }
        for (OrderDetail detail : orderDetailRepository.findByOrder_OrderId(orderId)) {
            int restored = ticketRepository.increaseRemainingQuantity(
                    detail.getTicket().getTicketId(), detail.getQuantity());
            if (restored == 0) {
                throw new ConflictException("Số liệu vé không nhất quán khi hủy đơn quá hạn " + orderId);
            }
        }
    }

    // ======================= HÀM PHỤ =======================

    /** Mã khách hàng đang đăng nhập (lấy từ session, không nhận từ client). Chỉ Customer được dùng. */
    private String currentCustomerId() {
        if (!CurrentUser.isCustomer()) {
            throw new ForbiddenException("Chỉ khách hàng mới được thao tác với đơn hàng.");
        }
        return CurrentUser.getId();
    }

    /** Tìm đơn và kiểm tra thuộc về khách đang đăng nhập. */
    private Order findOwnedOrder(String orderId, String customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("Bạn không có quyền truy cập đơn hàng này.");
        }
        return order;
    }

    /** Đổi entity sang DTO. Gọi trong transaction vì customer, ticket, schedule, event đều là LAZY. */
    OrderResponse toResponse(Order order, List<OrderDetail> details) { // package-private: AdminOrderService dùng chung
        List<OrderDetailResponse> items = new ArrayList<>();
        for (OrderDetail d : details) {
            OrderDetailResponse res = new OrderDetailResponse();
            res.setTicketId(d.getTicket().getTicketId());
            res.setTicketName(d.getTicket().getTicketName());
            res.setEventName(d.getTicket().getSchedule().getEvent().getTitle());
            res.setQuantity(d.getQuantity());
            res.setPrice(d.getPrice());
            res.setSubTotal(d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
            items.add(res);
        }

        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setCustomerId(order.getCustomer().getId());
        response.setCustomerName(order.getCustomer().getName());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setCreatedAt(order.getCreatedAt());
        response.setPaymentAt(order.getPaymentAt());
        response.setRefundReason(order.getRefundReason());
        response.setRefundRequestedAt(order.getRefundRequestedAt());
        response.setRefundResponse(order.getRefundResponse());
        if (STATUS_PENDING.equals(order.getStatus()) && order.getCreatedAt() != null) {
            response.setExpiresAt(order.getCreatedAt().plusMinutes(PENDING_TIMEOUT_MINUTES));
        }
        if (STATUS_PAID.equals(order.getStatus())) {
            response.setRefundableUntil(refundDeadline(details));
        }
        response.setDetails(items);
        return response;
    }
}
