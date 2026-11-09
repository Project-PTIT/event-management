package eventmanagement.dto;

import eventmanagement.entity.Event;

/** Dữ liệu sự kiện trả về cho danh sách và chi tiết. */
public record EventResponse(
        String eventId,
        String organizerId,
        String organizerName,
        String title,
        String image,
        String desc,
        String createType,
        String status) {

    /** Gọi bên trong transaction vì organizer là LAZY. */
    public static EventResponse from(Event e) {
        return new EventResponse(
                e.getEventId(),
                e.getOrganizer().getId(),
                e.getOrganizer().getName(),
                e.getTitle(),
                e.getImage(),
                e.getDesc(),
                e.getCreateType(),
                e.getStatus());
    }
}
