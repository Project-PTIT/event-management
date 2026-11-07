package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.EventResponse;
import eventmanagement.dto.PageResponse;
import eventmanagement.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** API công khai: danh sách, tìm kiếm, chi tiết sự kiện. */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ApiResponse<PageResponse<EventResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String createType,
            @RequestParam(required = false) String wardId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success("Lấy danh sách sự kiện thành công.",
                eventService.searchPublic(keyword, createType, wardId, fromDate, toDate, page, size));
    }

    @GetMapping("/{eventId}")
    public ApiResponse<EventResponse> detail(@PathVariable String eventId) {
        return ApiResponse.success("Lấy chi tiết sự kiện thành công.", eventService.getDetail(eventId));
    }
}
