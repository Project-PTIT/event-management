package eventmanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Dữ liệu tạo/sửa suất diễn. JSON: { venueId, startDatetime, endDatetime }.
 * Tên trường khớp cột start_datetime / end_datetime và entity EventSchedule.
 */
@Data
public class ScheduleRequest {
    private String venueId;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
}
