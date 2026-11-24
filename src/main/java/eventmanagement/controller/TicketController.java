package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.TicketRequestDTO;
import eventmanagement.dto.TicketResponse;
import eventmanagement.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API hạng vé. Phân quyền do SecurityConfig quyết định theo đường dẫn:
 * - GET /api/schedules/{scheduleId}/tickets: công khai.
 * - /api/organizer/**: chỉ ROLE_ORGANIZER; service còn kiểm tra vé thuộc sự kiện của chính Organizer.
 * - /api/tickets/**: chỉ ROLE_ADMIN (thống kê/danh sách cũ).
 * Không bắt Exception ở đây: lỗi được GlobalExceptionHandler đổi sang đúng mã 400/401/403/404.
 */
@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // ===================== Công khai =====================

    /** GET /api/schedules/{scheduleId}/tickets - các hạng vé của một suất diễn. */
    @GetMapping("/api/schedules/{scheduleId}/tickets")
    public ApiResponse<List<TicketResponse>> getTicketsBySchedule(@PathVariable String scheduleId) {
        return ApiResponse.success("Lấy danh sách hạng vé thành công", ticketService.getTicketsBySchedule(scheduleId));
    }

    // ===================== Organizer =====================

    /** POST /api/organizer/schedules/{scheduleId}/tickets - tạo hạng vé. */
    @PostMapping("/api/organizer/schedules/{scheduleId}/tickets")
    public ApiResponse<TicketResponse> createTicket(@PathVariable String scheduleId,
                                                    @RequestBody TicketRequestDTO request) {
        return ApiResponse.success("Tạo hạng vé thành công", ticketService.createTicket(scheduleId, request));
    }

    /** PUT /api/organizer/tickets/{ticketId} - sửa hạng vé. */
    @PutMapping("/api/organizer/tickets/{ticketId}")
    public ApiResponse<TicketResponse> updateTicket(@PathVariable String ticketId,
                                                    @RequestBody TicketRequestDTO request) {
        return ApiResponse.success("Cập nhật hạng vé thành công", ticketService.updateTicket(ticketId, request));
    }

    /** DELETE /api/organizer/tickets/{ticketId} - xóa hạng vé chưa có đơn hàng. */
    @DeleteMapping("/api/organizer/tickets/{ticketId}")
    public ApiResponse<Void> deleteTicket(@PathVariable String ticketId) {
        ticketService.deleteTicket(ticketId);
        return ApiResponse.success("Xóa hạng vé thành công", null);
    }

    // ===================== Admin (chức năng thống kê cũ, chỉ đọc) =====================

    /** GET /api/tickets - tất cả hạng vé. */
    @GetMapping("/api/tickets")
    public ApiResponse<List<TicketResponse>> getAllTickets() {
        return ApiResponse.success("Lấy danh sách vé thành công", ticketService.getAllTickets());
    }

    /** GET /api/tickets/stats/count - tổng số hạng vé. */
    @GetMapping("/api/tickets/stats/count")
    public ApiResponse<Long> getTicketCount() {
        return ApiResponse.success("Lấy tổng số vé thành công", ticketService.getTicketCount());
    }

    /** GET /api/tickets/details - danh sách vé kèm sự kiện và địa điểm. */
    @GetMapping("/api/tickets/details")
    public ApiResponse<List<Object[]>> getAllTicketsWithDetails() {
        return ApiResponse.success("Lấy danh sách vé chi tiết thành công", ticketService.getAllTicketsWithDetails());
    }

    /** GET /api/tickets/stats/top-events - thống kê suất diễn có nhiều hạng vé nhất. */
    @GetMapping("/api/tickets/stats/top-events")
    public ApiResponse<List<Object[]>> getTopEvents() {
        return ApiResponse.success("Lấy thống kê sự kiện thành công", ticketService.getTopEvents());
    }

    /** GET /api/tickets/stats/revenue - doanh thu theo tháng (đơn đã thanh toán). */
    @GetMapping("/api/tickets/stats/revenue")
    public ApiResponse<List<Object[]>> getRevenueByMonth() {
        return ApiResponse.success("Lấy thống kê doanh thu thành công", ticketService.getRevenueByMonth());
    }

    /** GET /api/tickets/{ticketId} - chi tiết một hạng vé (đặt sau các đường dẫn tĩnh). */
    @GetMapping("/api/tickets/{ticketId}")
    public ApiResponse<TicketResponse> getTicketById(@PathVariable String ticketId) {
        return ApiResponse.success("Lấy chi tiết vé thành công", ticketService.getTicketById(ticketId));
    }
}
