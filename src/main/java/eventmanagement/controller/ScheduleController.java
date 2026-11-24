package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.ScheduleArtistRequest;
import eventmanagement.dto.ScheduleArtistResponse;
import eventmanagement.dto.ScheduleRequest;
import eventmanagement.dto.ScheduleResponse;
import eventmanagement.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API suất diễn. Đường dẫn đầy đủ nằm ở từng phương thức để khớp 1-1 với SecurityConfig và JavaScript:
 * - /api/events/{eventId}/schedules, /api/schedules/{scheduleId}: công khai (chỉ GET).
 * - /api/organizer/**: chỉ ROLE_ORGANIZER (SecurityConfig), service còn kiểm tra quyền sở hữu sự kiện.
 */
@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /** GET /api/events/{eventId}/schedules - danh sách suất diễn của một sự kiện. */
    @GetMapping("/api/events/{eventId}/schedules")
    public ApiResponse<List<ScheduleResponse>> getSchedulesByEventId(@PathVariable String eventId) {
        return ApiResponse.success("Lấy danh sách suất diễn thành công",
                scheduleService.getSchedulesByEventId(eventId));
    }

    /** GET /api/schedules/{scheduleId} - chi tiết suất diễn. */
    @GetMapping("/api/schedules/{scheduleId}")
    public ApiResponse<ScheduleResponse> getScheduleById(@PathVariable String scheduleId) {
        return ApiResponse.success("Lấy chi tiết suất diễn thành công", scheduleService.getScheduleById(scheduleId));
    }

    /** POST /api/organizer/events/{eventId}/schedules - tạo suất diễn mới. */
    @PostMapping("/api/organizer/events/{eventId}/schedules")
    public ApiResponse<ScheduleResponse> createSchedule(@PathVariable String eventId,
                                                        @RequestBody ScheduleRequest request) {
        return ApiResponse.success("Tạo suất diễn mới thành công", scheduleService.createSchedule(eventId, request));
    }

    /** PUT /api/organizer/schedules/{scheduleId} - cập nhật suất diễn. */
    @PutMapping("/api/organizer/schedules/{scheduleId}")
    public ApiResponse<ScheduleResponse> updateSchedule(@PathVariable String scheduleId,
                                                        @RequestBody ScheduleRequest request) {
        return ApiResponse.success("Cập nhật suất diễn thành công", scheduleService.updateSchedule(scheduleId, request));
    }

    /** POST /api/organizer/schedules/{scheduleId}/artists - gán nghệ sĩ vào suất diễn. */
    @PostMapping("/api/organizer/schedules/{scheduleId}/artists")
    public ApiResponse<ScheduleArtistResponse> assignArtist(@PathVariable String scheduleId,
                                                            @RequestBody ScheduleArtistRequest request) {
        return ApiResponse.success("Gán nghệ sĩ vào suất diễn thành công",
                scheduleService.assignArtistToSchedule(scheduleId, request));
    }
}
