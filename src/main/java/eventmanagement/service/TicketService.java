package eventmanagement.service;

import eventmanagement.dto.TicketRequestDTO;
import eventmanagement.dto.TicketResponse;

import java.util.List;

/** Nghiệp vụ hạng vé (ticket) và các thống kê vé cho Admin. */
public interface TicketService {

    /** Danh sách hạng vé của một suất diễn (công khai nếu sự kiện đã APPROVED). */
    List<TicketResponse> getTicketsBySchedule(String scheduleId);

    /** Organizer tạo hạng vé cho suất diễn thuộc sự kiện của mình. */
    TicketResponse createTicket(String scheduleId, TicketRequestDTO dto);

    /** Organizer sửa hạng vé thuộc sự kiện của mình. */
    TicketResponse updateTicket(String ticketId, TicketRequestDTO dto);

    /** Organizer xóa hạng vé chưa phát sinh đơn hàng. */
    void deleteTicket(String ticketId);

    // ---- Dành cho Admin (giữ lại các chức năng thống kê cũ) ----
    List<TicketResponse> getAllTickets();

    TicketResponse getTicketById(String ticketId);

    List<Object[]> getAllTicketsWithDetails();

    List<Object[]> getTopEvents();

    List<Object[]> getRevenueByMonth();

    long getTicketCount();
}
