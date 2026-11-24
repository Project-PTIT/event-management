package eventmanagement.repository;

import eventmanagement.entity.OrderDetail;
import eventmanagement.entity.OrderDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId> {

    // Tìm chi tiết đơn theo mã đơn hàng (sắp theo mã vé để thứ tự ổn định)
    @Query("select d from OrderDetail d where d.order.orderId = :orderId order by d.ticket.ticketId")
    List<OrderDetail> findByOrder_OrderId(@Param("orderId") String orderId);

    /** Vé đã có trong bất kỳ đơn hàng nào chưa (dùng để chặn xóa vé). */
    boolean existsByTicket_TicketId(String ticketId);

    /**
     * Doanh thu và số vé đã bán (chỉ đơn 'Đã thanh toán') của từng sự kiện thuộc một Organizer.
     * Mỗi dòng: [eventId, doanh thu (BigDecimal), số vé (Long)]. Sự kiện chưa bán được vé không xuất hiện.
     */
    @Query("select e.eventId, sum(od.price * od.quantity), sum(od.quantity) " +
            "from OrderDetail od join od.order o join od.ticket t join t.schedule s join s.event e " +
            "where o.status in ('Đã thanh toán', 'Chờ hoàn tiền') and e.organizer.id = :organizerId " +
            "group by e.eventId")
    List<Object[]> sumSalesByEventForOrganizer(@Param("organizerId") String organizerId);
}
