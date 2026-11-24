package eventmanagement.dto;

import eventmanagement.entity.EventSchedule;
import eventmanagement.entity.Ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Hạng vé trả về frontend.
 * JSON: { ticketId, scheduleId, ticketName, desc, price, totalQuantity, remainingQuantity,
 *         eventTitle, startDatetime, venueName }.
 */
public record TicketResponse(
        String ticketId,
        String scheduleId,
        String ticketName,
        String desc,
        BigDecimal price,
        Integer totalQuantity,
        Integer remainingQuantity,
        String eventTitle,
        LocalDateTime startDatetime,
        String venueName) {

    /** Gọi trong transaction vì schedule, event, venue là LAZY. */
    public static TicketResponse from(Ticket ticket) {
        EventSchedule schedule = ticket.getSchedule();
        return new TicketResponse(
                ticket.getTicketId(),
                schedule.getScheduleId(),
                ticket.getTicketName(),
                ticket.getDesc(),
                ticket.getPrice(),
                ticket.getTotalQuantity(),
                ticket.getRemainingQuantity(),
                schedule.getEvent().getTitle(),
                schedule.getStartDatetime(),
                schedule.getVenue().getVenueName());
    }
}
