package eventmanagement.repository;

import eventmanagement.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {

    @Query(value = "SELECT t.ticket_id, t.ticket_name, t.price, t.total_quantity, t.remaining_quantity, " +
                   "e.title AS event_title, es.start_datetime, v.venue_name " +
                   "FROM ticket t " +
                   "JOIN event_schedule es ON t.schedule_id = es.schedule_id " +
                   "JOIN event e ON es.event_id = e.event_id " +
                   "JOIN venue v ON es.venue_id = v.venue_id", nativeQuery = true)
    List<Object[]> findAllTicketsWithDetails();

    @Query(value = "SELECT schedule_id, COUNT(*) FROM ticket GROUP BY schedule_id ORDER BY COUNT(*) DESC", nativeQuery = true)
    List<Object[]> getTopEvents();

    @Query(value = "SELECT MONTH(o.created_at) as month, SUM(od.price * od.quantity) as revenue " +
                   "FROM `order` o JOIN order_detail od ON o.order_id = od.order_id " +
                   "WHERE o.status = 'Đã thanh toán' " +
                   "GROUP BY MONTH(o.created_at) ORDER BY month ASC", nativeQuery = true)
    List<Object[]> getRevenueByMonth();
}