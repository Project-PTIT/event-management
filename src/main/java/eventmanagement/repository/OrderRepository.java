package eventmanagement.repository;

import eventmanagement.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    // Customer kế thừa People nên thuộc tính khóa là 'id'
    List<Order> findByCustomer_Id(String customerId);

    // Phục vụ /api/orders/me: sắp xếp mới nhất trước và lọc theo trạng thái
    List<Order> findByCustomer_IdOrderByCreatedAtDesc(String customerId);

    List<Order> findByCustomer_IdAndStatusOrderByCreatedAtDesc(String customerId, String status);

    /**
     * Chuyển đơn 'Chờ thanh toán' sang 'Đã thanh toán' trong MỘT câu UPDATE có điều kiện.
     * Trả 1 nếu thành công, 0 nếu đơn không còn ở trạng thái chờ (đã thanh toán/đã hủy/không phải của khách này).
     * payment_at dùng giờ của DB và không bao giờ nhỏ hơn created_at (đúng CHECK của bảng order).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE `order` SET `status` = 'Đã thanh toán', payment_method = :method, " +
            "payment_at = GREATEST(NOW(), created_at) " +
            "WHERE order_id = :orderId AND customer_id = :customerId AND `status` = 'Chờ thanh toán'",
            nativeQuery = true)
    int markPaid(@Param("orderId") String orderId,
                 @Param("customerId") String customerId,
                 @Param("method") String method);

    /**
     * Chuyển đơn 'Chờ thanh toán' sang 'Đã hủy' trong MỘT câu UPDATE có điều kiện.
     * Hai request hủy đồng thời: chỉ một request nhận được 1, request còn lại nhận 0
     * nên vé chỉ được hoàn đúng một lần.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE `order` SET `status` = 'Đã hủy', payment_at = NULL " +
            "WHERE order_id = :orderId AND customer_id = :customerId AND `status` = 'Chờ thanh toán'",
            nativeQuery = true)
    int markCancelled(@Param("orderId") String orderId, @Param("customerId") String customerId);

    /** Mã các đơn 'Chờ thanh toán' tạo trước mốc cutoff (đơn quá hạn giữ chỗ). */
    @Query("select o.orderId from eventmanagement.entity.Order o where o.status = 'Chờ thanh toán' and o.createdAt < :cutoff")
    List<String> findExpiredPendingIds(@Param("cutoff") java.time.LocalDateTime cutoff);

    /** Hủy đơn quá hạn (hệ thống hủy, không cần customerId). Trả 0 nếu đơn không còn ở trạng thái chờ. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE `order` SET `status` = 'Đã hủy', payment_at = NULL " +
            "WHERE order_id = :orderId AND `status` = 'Chờ thanh toán'", nativeQuery = true)
    int markExpiredCancelled(@Param("orderId") String orderId);

    // ---------------- Dành cho Admin ----------------

    /** Tất cả đơn hàng, mới nhất trước. */
    List<Order> findAllByOrderByCreatedAtDesc();

    /** Đơn hàng theo trạng thái, mới nhất trước. */
    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Hoàn tiền: chỉ đơn 'Đã thanh toán' mới chuyển sang 'Đã hoàn tiền' (giữ payment_at đúng CHECK của bảng order).
     * Trả 1 nếu thành công, 0 nếu đơn không ở trạng thái đã thanh toán - nên hoàn vé đúng một lần dù bấm hai lần.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE `order` SET `status` = 'Đã hoàn tiền', refund_response = COALESCE(:note, refund_response) " +
            "WHERE order_id = :orderId AND `status` IN ('Đã thanh toán', 'Chờ hoàn tiền')",
            nativeQuery = true)
    int markRefunded(@Param("orderId") String orderId, @Param("note") String note);

    /** Khách gửi yêu cầu hoàn tiền: chỉ đơn 'Đã thanh toán' của chính khách đó. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE `order` SET `status` = 'Chờ hoàn tiền', refund_reason = :reason, " +
            "refund_requested_at = NOW(), refund_response = NULL " +
            "WHERE order_id = :orderId AND customer_id = :customerId AND `status` = 'Đã thanh toán'",
            nativeQuery = true)
    int markRefundRequested(@Param("orderId") String orderId, @Param("customerId") String customerId,
                            @Param("reason") String reason);

    /** Khách rút lại yêu cầu hoàn tiền: về lại 'Đã thanh toán'. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE `order` SET `status` = 'Đã thanh toán', refund_reason = NULL, refund_requested_at = NULL " +
            "WHERE order_id = :orderId AND customer_id = :customerId AND `status` = 'Chờ hoàn tiền'",
            nativeQuery = true)
    int markRefundRequestWithdrawn(@Param("orderId") String orderId, @Param("customerId") String customerId);

    /** Admin từ chối yêu cầu hoàn tiền: đơn về 'Đã thanh toán', lưu lý do từ chối. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE `order` SET `status` = 'Đã thanh toán', refund_response = :note " +
            "WHERE order_id = :orderId AND `status` = 'Chờ hoàn tiền'", nativeQuery = true)
    int markRefundRejected(@Param("orderId") String orderId, @Param("note") String note);

    /** Tổng doanh thu của các đơn đã thanh toán (đơn đã hoàn tiền/đã hủy không tính). */
    @Query("select sum(o.totalAmount) from eventmanagement.entity.Order o where o.status in ('Đã thanh toán', 'Chờ hoàn tiền')")
    java.math.BigDecimal sumPaidRevenueOrNull();

    /** Tổng doanh thu đã thanh toán; chưa có đơn nào thì là 0 (SUM của tập rỗng trả NULL). */
    default java.math.BigDecimal sumPaidRevenue() {
        java.math.BigDecimal total = sumPaidRevenueOrNull();
        return total == null ? java.math.BigDecimal.ZERO : total;
    }
}
