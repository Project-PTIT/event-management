package eventmanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Suất diễn trả về frontend.
 * JSON: { scheduleId, eventId, startDatetime, endDatetime, status, venue{...}, artists[{artistId, artistName, image, role}] }.
 */
@Data
public class ScheduleResponse {
    private String scheduleId;
    private String eventId;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String status;
    private VenueResponse venue;
    private List<ScheduleArtistResponse> artists;
}
