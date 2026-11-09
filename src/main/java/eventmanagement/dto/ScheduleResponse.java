package eventmanagement.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScheduleResponse {
    private String id;
    private String scheduleName;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String status;

    private VenueResponse venue;
    private List<ScheduleArtistResponse> artists;
}