package eventmanagement.controller;

import eventmanagement.dto.AdminStatisticsResponse;
import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.OrganizerStatisticsResponse;
import eventmanagement.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API thống kê. Phân quyền theo đường dẫn ở SecurityConfig:
 * /api/admin/** chỉ ROLE_ADMIN, /api/organizer/** chỉ ROLE_ORGANIZER; service còn kiểm tra lại role.
 */
@RestController
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /** GET /api/admin/statistics/overview - tổng quan hệ thống. */
    @GetMapping("/api/admin/statistics/overview")
    public ApiResponse<AdminStatisticsResponse> adminOverview() {
        return ApiResponse.success("Lấy thống kê tổng quan thành công", statisticsService.adminOverview());
    }

    /** GET /api/organizer/statistics/overview - tổng quan bán vé của Organizer đang đăng nhập. */
    @GetMapping("/api/organizer/statistics/overview")
    public ApiResponse<OrganizerStatisticsResponse.Overview> organizerOverview() {
        return ApiResponse.success("Lấy thống kê tổng quan thành công", statisticsService.organizerOverview());
    }

    /** GET /api/organizer/statistics/by-event - doanh thu và số vé theo từng sự kiện. */
    @GetMapping("/api/organizer/statistics/by-event")
    public ApiResponse<List<OrganizerStatisticsResponse.ByEvent>> organizerByEvent() {
        return ApiResponse.success("Lấy thống kê theo sự kiện thành công", statisticsService.organizerByEvent());
    }
}
