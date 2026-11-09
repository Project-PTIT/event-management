package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.ScheduleResponse;
import eventmanagement.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // GET /api/schedules/{id} - Lấy chi tiết suất diễn
    @GetMapping("/{id}")
    public ApiResponse<ScheduleResponse> getScheduleById(@PathVariable String id) {
        ScheduleResponse data = scheduleService.getScheduleById(id);
        return ApiResponse.success("Lấy chi tiết suất diễn thành công", data);
    }
    // POST /api/organizer/events/{eventId}/schedules - Tạo suất diễn mới
    @PostMapping("/organizer/events/{eventId}")
    public ApiResponse<ScheduleResponse> createSchedule(@PathVariable String eventId,
                                                        @RequestBody eventmanagement.dto.ScheduleRequest request) {
        ScheduleResponse data = scheduleService.createSchedule(eventId, request);
        return ApiResponse.success("Tạo suất diễn mới thành công", data);
    }
    // GET /api/events/{eventId}/schedules - Lấy danh sách suất diễn của một sự kiện
    @GetMapping("/events/{eventId}/schedules")
    public ApiResponse<List<ScheduleResponse>> getSchedulesByEventId(@PathVariable String eventId) {
        List<ScheduleResponse> data = scheduleService.getSchedulesByEventId(eventId);
        return ApiResponse.success("Lấy danh sách suất diễn thành công", data);
    }
    // POST /api/schedules/organizer/{scheduleId}/artists - Gán nghệ sĩ
    @PostMapping("/organizer/{scheduleId}/artists")
    public ApiResponse<eventmanagement.dto.ScheduleArtistResponse> assignArtistToSchedule(
            @PathVariable String scheduleId,
            @RequestBody eventmanagement.dto.ScheduleArtistRequest request) {

        eventmanagement.dto.ScheduleArtistResponse data = scheduleService.assignArtistToSchedule(scheduleId, request);
        return ApiResponse.success("Gán nghệ sĩ vào suất diễn thành công", data);
    }
    // PUT /api/organizer/schedules/{scheduleId} - Cập nhật suất diễn
    @PutMapping("/organizer/{scheduleId}")
    public ApiResponse<ScheduleResponse> updateSchedule(@PathVariable String scheduleId,
                                                        @RequestBody eventmanagement.dto.ScheduleRequest request) {
        ScheduleResponse data = scheduleService.updateSchedule(scheduleId, request);
        return ApiResponse.success("Cập nhật suất diễn thành công", data);
    }
}