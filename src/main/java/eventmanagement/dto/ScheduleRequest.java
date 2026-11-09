package eventmanagement.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScheduleRequest {
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String venueId;
}