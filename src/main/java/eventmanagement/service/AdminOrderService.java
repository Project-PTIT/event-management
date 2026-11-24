package eventmanagement.service;

import eventmanagement.dto.OrderResponse;
import eventmanagement.entity.Order;
import eventmanagement.entity.OrderDetail;
import eventmanagement.exception.BadRequestException;
import eventmanagement.exception.ConflictException;
import eventmanagement.exception.ResourceNotFoundException;
import eventmanagement.repository.OrderDetailRepository;
import eventmanagement.repository.OrderRepository;
import eventmanagement.repository.TicketRepository;
import eventmanagement.util.CurrentUser;
import eventmanagement.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Quản lý đơn hàng cho Admin: xem tất cả đơn và hoàn tiền đơn đã thanh toán.
 * Có @Transactional vì entity -> DTO đi qua nhiều quan hệ LAZY (open-in-view = false).
 */
@Service
@RequiredArgsConstructor
public class AdminOrderService {

    // Trạng thái hợp lệ của đơn (khớp CHECK của bảng order)
    private static final Set<String> STATUSES = Set.of("Chờ thanh toán", "Đã thanh toán", "Đã hủy", "Đã hoàn tiền", "Chờ hoàn tiền");

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final TicketRepository ticketRepository;
    private final OrderService orderService; // dùng lại hàm đổi entity -> DTO

    /** Danh sách đơn hàng, có thể lọc theo trạng thái. */
    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(String status) {
        requireAdmin();
        List<Order> orders;
        if (status == null || status.isBlank()) {
            orders = orderRepository.findAllByOrderByCreatedAtDesc();
        } else {
            String st = status.trim();
            if (!STATUSES.contains(st)) {
                throw new BadRequestException("Trạng thái đơn hàng không hợp lệ: " + st);
            }
            orders = orderRepository.findByStatusOrderByCreatedAtDesc(st);
        }
        return orders.stream()
                .map(o -> orderService.toResponse(o, orderDetailRepository.findByOrder_OrderId(o.getOrderId())))
                .toList();
    }

    /**
     * Hoàn tiền một đơn đã thanh toán và hoàn vé về kho đúng một lần.
     * Bước đổi trạng thái là UPDATE có điều kiện nên hai request cùng lúc không thể hoàn vé hai lần;
     * bước hoàn vé lỗi thì rollback cả transaction.
     */
    @Transactional
    public OrderResponse refundOrder(String orderId, String note) {
        requireAdmin();
        orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));

        String cleanNote = (note == null || note.isBlank()) ? null : note.trim();
        if (orderRepository.markRefunded(orderId, cleanNote) == 0) {
            throw new BadRequestException("Chỉ hoàn tiền được đơn 'Đã thanh toán' hoặc 'Chờ hoàn tiền'.");
        }

        for (OrderDetail detail : orderDetailRepository.findByOrder_OrderId(orderId)) {
            int restored = ticketRepository.increaseRemainingQuantity(
                    detail.getTicket().getTicketId(), detail.getQuantity());
            if (restored == 0) {
                throw new ConflictException("Không thể hoàn vé cho đơn " + orderId
                        + " vì số liệu vé không nhất quán. Thao tác đã được hoàn tác.");
            }
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));
        return orderService.toResponse(order, orderDetailRepository.findByOrder_OrderId(orderId));
    }

    /** Từ chối yêu cầu hoàn tiền của khách: đơn về 'Đã thanh toán', bắt buộc có lý do. */
    @Transactional
    public OrderResponse rejectRefund(String orderId, String note) {
        requireAdmin();
        orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));
        String text = note == null ? "" : note.trim();
        if (text.length() < 3 || text.length() > 500) {
            throw new BadRequestException("Vui lòng nhập lý do từ chối (3 đến 500 ký tự).");
        }
        if (orderRepository.markRefundRejected(orderId, text) == 0) {
            throw new BadRequestException("Đơn không ở trạng thái 'Chờ hoàn tiền'.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));
        return orderService.toResponse(order, orderDetailRepository.findByOrder_OrderId(orderId));
    }

    /** Phòng thủ thêm: SecurityConfig đã chặn /api/admin/** chỉ cho Admin. */
    private void requireAdmin() {
        if (!CurrentUser.isAdmin()) {
            throw new ForbiddenException("Chỉ quản trị viên mới được thao tác với đơn hàng của hệ thống.");
        }
    }
}
