package eventmanagement.repository;

import eventmanagement.entity.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, String> {

    // Method có sẵn
    List<Ticket> findBySchedule_ScheduleId(String scheduleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ticket> findByTicketId(String ticketId);

    // --- BE5 thêm 2 method cập nhật atomic theo quy ước đề tài ---

    @Modifying
    @Query("UPDATE Ticket t SET t.remainingQuantity = t.remainingQuantity - :quantity " +
           "WHERE t.ticketId = :ticketId AND t.remainingQuantity >= :quantity")
    int decreaseRemainingQuantity(@Param("ticketId") String ticketId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Ticket t SET t.remainingQuantity = t.remainingQuantity + :quantity " +
           "WHERE t.ticketId = :ticketId AND (t.remainingQuantity + :quantity) <= t.totalQuantity")
    int increaseRemainingQuantity(@Param("ticketId") String ticketId, @Param("quantity") int quantity);
}