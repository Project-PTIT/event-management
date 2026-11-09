package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.EventCreateRequest;
import eventmanagement.dto.EventResponse;
import eventmanagement.dto.EventUpdateRequest;
import eventmanagement.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API cho Organizer: sự kiện của mình, tạo, sửa. */
@RestController
@RequestMapping("/api/organizer/events")
@RequiredArgsConstructor
public class OrganizerEventController {

    private final EventService eventService;

    @GetMapping
    public ApiResponse<List<EventResponse>> listMine(@RequestParam(required = false) String status) {
        return ApiResponse.success("Lấy danh sách sự kiện của bạn thành công.", eventService.listMine(status));
    }

    @PostMapping
    public ApiResponse<EventResponse> create(@RequestBody EventCreateRequest request) {
        return ApiResponse.success("Tạo sự kiện thành công, đang chờ duyệt.", eventService.create(request));
    }

    @PutMapping("/{eventId}")
    public ApiResponse<EventResponse> update(@PathVariable String eventId,
                                             @RequestBody EventUpdateRequest request) {
        return ApiResponse.success("Cập nhật sự kiện thành công.", eventService.update(eventId, request));
    }
}
