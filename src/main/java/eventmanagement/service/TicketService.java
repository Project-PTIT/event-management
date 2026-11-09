package eventmanagement.service;

import eventmanagement.dto.TicketRequestDTO;
import eventmanagement.dto.TicketResponse;
import java.util.List;

public interface TicketService {
    TicketResponse createTicket(TicketRequestDTO dto);
    List<TicketResponse> getAllTickets();
    TicketResponse getTicketById(String id);
    TicketResponse updateTicket(String id, TicketRequestDTO dto);
    void deleteTicket(String id);
    List<Object[]> getAllTicketsWithDetails();
    List<Object[]> getTopEvents();
    List<Object[]> getRevenueByMonth();
    long getTicketCount();
}