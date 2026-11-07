package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.EventResponse;
import eventmanagement.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API cho Admin: danh sách theo trạng thái, duyệt, từ chối. */
@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public ApiResponse<List<EventResponse>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success("Lấy danh sách sự kiện thành công.", eventService.listForAdmin(status));
    }

    @PutMapping("/{eventId}/approve")
    public ApiResponse<EventResponse> approve(@PathVariable String eventId) {
        return ApiResponse.success("Đã duyệt sự kiện.", eventService.approve(eventId));
    }

    @PutMapping("/{eventId}/reject")
    public ApiResponse<EventResponse> reject(@PathVariable String eventId) {
        return ApiResponse.success("Đã từ chối sự kiện.", eventService.reject(eventId));
    }
}
