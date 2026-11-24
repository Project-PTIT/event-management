package eventmanagement.repository;

import eventmanagement.entity.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Truy vấn bảng ticket.
 * Các câu UPDATE trừ/hoàn vé đều có điều kiện ngay trong SQL để an toàn khi nhiều request chạy đồng thời.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {

    // ===================== Thống kê (giữ nguyên chức năng cũ, chỉ Admin được gọi) =====================

    @Query(value = "SELECT t.ticket_id, t.ticket_name, t.price, t.total_quantity, t.remaining_quantity, " +
            "e.title AS event_title, es.start_datetime, v.venue_name " +
            "FROM ticket t " +
            "JOIN event_schedule es ON t.schedule_id = es.schedule_id " +
            "JOIN `event` e ON es.event_id = e.event_id " +
            "JOIN venue v ON es.venue_id = v.venue_id", nativeQuery = true)
    List<Object[]> findAllTicketsWithDetails();

    @Query(value = "SELECT schedule_id, COUNT(*) FROM ticket GROUP BY schedule_id ORDER BY COUNT(*) DESC", nativeQuery = true)
    List<Object[]> getTopEvents();

    /** Trả các dòng [năm, tháng, doanh thu], tính theo ngày thanh toán, chỉ đơn 'Đã thanh toán'. */
    @Query(value = "SELECT YEAR(o.payment_at) AS yr, MONTH(o.payment_at) AS mth, SUM(od.price * od.quantity) AS revenue " +
            "FROM `order` o JOIN order_detail od ON o.order_id = od.order_id " +
            "WHERE o.status IN ('Đã thanh toán', 'Chờ hoàn tiền') AND o.payment_at IS NOT NULL " +
            "GROUP BY YEAR(o.payment_at), MONTH(o.payment_at) ORDER BY yr ASC, mth ASC", nativeQuery = true)
    List<Object[]> getRevenueByMonth();

    // ===================== Quản lý hạng vé =====================

    /** Danh sách hạng vé của một suất diễn, giá thấp đến cao. */
    List<Ticket> findBySchedule_ScheduleIdOrderByPriceAscTicketNameAsc(String scheduleId);

    /** Kiểm tra trùng tên hạng vé trong cùng suất diễn (bỏ qua hoa/thường), loại trừ chính vé đang sửa. */
    @Query("select count(t) > 0 from Ticket t " +
            "where t.schedule.scheduleId = :scheduleId " +
            "and lower(t.ticketName) = lower(:ticketName) " +
            "and t.ticketId <> :excludeTicketId")
    boolean existsDuplicateName(@Param("scheduleId") String scheduleId,
                                @Param("ticketName") String ticketName,
                                @Param("excludeTicketId") String excludeTicketId);

    /** Tổng số vé (total_quantity) của các hạng vé khác trong suất diễn; dùng để so với sức chứa địa điểm. */
    @Query("select sum(t.totalQuantity) from Ticket t " +
            "where t.schedule.scheduleId = :scheduleId and t.ticketId <> :excludeTicketId")
    Long sumTotalQuantityExcludingOrNull(@Param("scheduleId") String scheduleId,
                                         @Param("excludeTicketId") String excludeTicketId);

    /** Như trên nhưng trả 0 thay vì NULL khi suất diễn chưa có hạng vé nào. */
    default long sumTotalQuantityExcluding(String scheduleId, String excludeTicketId) {
        Long total = sumTotalQuantityExcludingOrNull(scheduleId, excludeTicketId);
        return total == null ? 0L : total;
    }

    /** Mã suất diễn của một vé (để khóa suất diễn trước khi khóa vé, giữ thứ tự khóa nhất quán). */
    @Query("select t.schedule.scheduleId from Ticket t where t.ticketId = :ticketId")
    Optional<String> findScheduleIdByTicketId(@Param("ticketId") String ticketId);

    /** Khóa dòng vé để sửa/xóa, tránh chạy song song với các thao tác khác trên cùng vé. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.ticketId = :ticketId")
    Optional<Ticket> findByIdForUpdate(@Param("ticketId") String ticketId);

    /**
     * Giá vé thấp nhất còn mở bán của từng sự kiện: chỉ tính suất diễn chưa bắt đầu và hạng vé còn vé.
     * Trả về các dòng [eventId, giá thấp nhất]; sự kiện không có hạng vé phù hợp sẽ không xuất hiện.
     */
    @Query("select s.event.eventId, min(t.price) from Ticket t join t.schedule s " +
            "where s.event.eventId in :eventIds and s.startDatetime > :now and t.remainingQuantity > 0 " +
            "group by s.event.eventId")
    List<Object[]> findMinPriceByEventIds(@Param("eventIds") java.util.Collection<String> eventIds,
                                          @Param("now") java.time.LocalDateTime now);

    // ===================== Trừ / hoàn vé khi đặt và hủy đơn =====================

    /**
     * Trừ vé. Chỉ thành công (trả 1) khi còn đủ số lượng; trả 0 nếu không đủ.
     * Điều kiện nằm trong SQL nên hai request đồng thời không thể bán vượt số vé còn lại.
     * Không clear persistence context để các entity đã nạp (ticket, schedule...) vẫn dùng được; chúng không bị sửa nên không bị ghi đè.
     */
    @Modifying(flushAutomatically = true)
    @Query(value = "UPDATE ticket SET remaining_quantity = remaining_quantity - :quantity " +
            "WHERE ticket_id = :ticketId AND remaining_quantity >= :quantity",
            nativeQuery = true)
    int decreaseRemainingQuantity(@Param("ticketId") String ticketId, @Param("quantity") int quantity);

    /**
     * Hoàn vé. Chỉ thành công (trả 1) khi sau khi cộng vẫn không vượt total_quantity;
     * trả 0 nếu việc hoàn sẽ làm remaining_quantity lớn hơn total_quantity.
     */
    @Modifying(flushAutomatically = true)
    @Query(value = "UPDATE ticket SET remaining_quantity = remaining_quantity + :quantity " +
            "WHERE ticket_id = :ticketId AND remaining_quantity + :quantity <= total_quantity",
            nativeQuery = true)
    int increaseRemainingQuantity(@Param("ticketId") String ticketId, @Param("quantity") int quantity);
}
