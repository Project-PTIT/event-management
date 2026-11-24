package eventmanagement.dto;

import eventmanagement.entity.Event;

import java.math.BigDecimal;

/** Dữ liệu sự kiện trả về cho danh sách và chi tiết. */
public record EventResponse(
        String eventId,
        String organizerId,
        String organizerName,
        String title,
        String image,
        String desc,
        String createType,
        String status,
        BigDecimal minPrice) {

    /** Gọi bên trong transaction vì organizer là LAZY. Không kèm giá (minPrice = null). */
    public static EventResponse from(Event e) {
        return from(e, null);
    }

    /**
     * Kèm giá vé thấp nhất còn mở bán (dùng ở danh sách công khai).
     * minPrice = null khi sự kiện chưa có hạng vé nào đang mở bán.
     */
    public static EventResponse from(Event e, BigDecimal minPrice) {
        return new EventResponse(
                e.getEventId(),
                e.getOrganizer().getId(),
                e.getOrganizer().getName(),
                e.getTitle(),
                e.getImage(),
                e.getDesc(),
                e.getCreateType(),
                e.getStatus(),
                minPrice);
    }
}
