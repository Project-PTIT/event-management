package eventmanagement.service.impl;

import eventmanagement.dto.TicketRequestDTO;
import eventmanagement.dto.TicketResponse;
import eventmanagement.entity.EventSchedule;
import eventmanagement.entity.Ticket;
import eventmanagement.repository.EventScheduleRepository;
import eventmanagement.repository.TicketRepository;
import eventmanagement.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventScheduleRepository eventScheduleRepository;

    // Chuyển đổi Entity sang DTO phẳng trong Transaction để tránh lỗi Session và Lặp JSON
    private TicketResponse convertToDTO(Ticket ticket) {
        EventSchedule schedule = ticket.getSchedule();
        return new TicketResponse(
            ticket.getTicketId(),
            ticket.getTicketName(),
            ticket.getPrice(),
            ticket.getTotalQuantity(),
            ticket.getRemainingQuantity(),
            (schedule != null && schedule.getEvent() != null) ? schedule.getEvent().getTitle() : "",
            schedule != null ? schedule.getStartDatetime() : null,
            (schedule != null && schedule.getVenue() != null) ? schedule.getVenue().getVenueName() : ""
        );
    }

    @Override
    @Transactional
    public TicketResponse createTicket(TicketRequestDTO dto) {
        EventSchedule schedule = eventScheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch trình: " + dto.getScheduleId()));

        Ticket ticket = new Ticket();
        ticket.setTicketId(dto.getTicketId());
        ticket.setTicketName(dto.getTicketName());
        ticket.setPrice(dto.getPrice());
        ticket.setDesc(dto.getDesc());
        ticket.setTotalQuantity(dto.getTotalQuantity());
        ticket.setRemainingQuantity(dto.getRemainingQuantity());
        ticket.setSchedule(schedule);

        Ticket savedTicket = ticketRepository.save(ticket);
        return convertToDTO(savedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(String id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé: " + id));
        return convertToDTO(ticket);
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(String id, TicketRequestDTO dto) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé: " + id));
        EventSchedule schedule = eventScheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch trình: " + dto.getScheduleId()));

        ticket.setTicketName(dto.getTicketName());
        ticket.setPrice(dto.getPrice());
        ticket.setDesc(dto.getDesc());
        ticket.setTotalQuantity(dto.getTotalQuantity());
        ticket.setRemainingQuantity(dto.getRemainingQuantity());
        ticket.setSchedule(schedule);

        Ticket updatedTicket = ticketRepository.save(ticket);
        return convertToDTO(updatedTicket);
    }

    @Override
    @Transactional
    public void deleteTicket(String id) {
        ticketRepository.deleteById(id);
    }

    @Override
    public List<Object[]> getAllTicketsWithDetails() {
        return ticketRepository.findAllTicketsWithDetails();
    }

    @Override
    public List<Object[]> getTopEvents() {
        return ticketRepository.getTopEvents();
    }

    @Override
    public List<Object[]> getRevenueByMonth() {
        return ticketRepository.getRevenueByMonth();
    }

    @Override
    public long getTicketCount() {
        return ticketRepository.count();
    }
}