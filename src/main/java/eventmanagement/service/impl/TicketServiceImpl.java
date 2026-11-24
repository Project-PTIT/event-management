package eventmanagement.service.impl;

import eventmanagement.dto.TicketRequestDTO;
import eventmanagement.dto.TicketResponse;
import eventmanagement.entity.Event;
import eventmanagement.entity.EventSchedule;
import eventmanagement.entity.Ticket;
import eventmanagement.exception.BadRequestException;
import eventmanagement.exception.ResourceNotFoundException;
import eventmanagement.repository.EventScheduleRepository;
import eventmanagement.repository.OrderDetailRepository;
import eventmanagement.repository.TicketRepository;
import eventmanagement.service.TicketService;
import eventmanagement.util.AccessRules;
import eventmanagement.util.AppTime;
import eventmanagement.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Quản lý hạng vé. Mọi phương thức có @Transactional vì entity -> DTO đi qua các quan hệ LAZY.
 * Thứ tự khóa luôn là: khóa suất diễn trước, khóa vé sau, để tránh deadlock giữa các request.
 */
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final int MAX_NAME = 200;   // ticket.ticket_name VARCHAR(200)
    private static final int MAX_DESC = 2000;  // ticket.desc VARCHAR(2000)
    // price là DECIMAL(15,0): tối đa 15 chữ số, không có phần thập phân
    private static final BigDecimal MAX_PRICE = new BigDecimal("999999999999999");

    private final TicketRepository ticketRepository;
    private final EventScheduleRepository scheduleRepository;
    private final OrderDetailRepository orderDetailRepository;

    // ======================= XEM =======================

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsBySchedule(String scheduleId) {
        EventSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất diễn với ID: " + scheduleId));
        // Sự kiện chưa duyệt chỉ chủ sự kiện/Admin xem được; người khác nhận 404 để không lộ dữ liệu
        if (!AccessRules.canView(schedule.getEvent())) {
            throw new ResourceNotFoundException("Không tìm thấy suất diễn với ID: " + scheduleId);
        }
        return ticketRepository.findBySchedule_ScheduleIdOrderByPriceAscTicketNameAsc(scheduleId).stream()
                .map(TicketResponse::from)
                .toList();
    }

    // ======================= TẠO =======================

    @Override
    @Transactional
    public TicketResponse createTicket(String scheduleId, TicketRequestDTO dto) {
        ValidatedTicket input = validate(dto);

        // Khóa suất diễn để kiểm tra sức chứa không bị vượt khi hai request tạo vé cùng lúc
        EventSchedule schedule = scheduleRepository.findByIdForUpdate(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất diễn với ID: " + scheduleId));
        Event event = schedule.getEvent();
        AccessRules.requireOwner(event); // Organizer chỉ tạo vé cho sự kiện của mình
        requireUsableSchedule(schedule);

        // Khi tạo mới, số còn lại luôn bằng tổng số vé; nếu client gửi giá trị khác thì từ chối
        if (input.remaining() != null && !input.remaining().equals(input.total())) {
            throw new BadRequestException("Khi tạo hạng vé, số vé còn lại phải bằng tổng số vé.");
        }

        checkUniqueName(scheduleId, input.name(), "");
        checkCapacity(schedule, input.total(), "");

        Ticket ticket = new Ticket();
        ticket.setTicketId(IdGenerator.nextId("TK", 3)); // TK001, TK002...
        ticket.setSchedule(schedule);
        ticket.setTicketName(input.name());
        ticket.setDesc(input.desc());
        ticket.setPrice(input.price());
        ticket.setTotalQuantity(input.total());
        ticket.setRemainingQuantity(input.total());
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    // ======================= SỬA =======================

    @Override
    @Transactional
    public TicketResponse updateTicket(String ticketId, TicketRequestDTO dto) {
        ValidatedTicket input = validate(dto);
        Ticket ticket = lockTicketAndSchedule(ticketId);
        EventSchedule schedule = ticket.getSchedule();
        AccessRules.requireOwner(schedule.getEvent()); // Organizer A không sửa được vé của Organizer B
        requireUsableSchedule(schedule);

        // Số vé đã bán (kể cả đơn đang chờ thanh toán) = tổng cũ - còn lại cũ
        int sold = ticket.getTotalQuantity() - ticket.getRemainingQuantity();
        if (input.total() < sold) {
            throw new BadRequestException("Không thể giảm tổng số vé xuống dưới số vé đã bán (" + sold + ").");
        }
        // Số còn lại do server tính; không cho ghi đè tùy ý
        int newRemaining = input.total() - sold;
        if (input.remaining() != null && input.remaining() != newRemaining) {
            throw new BadRequestException("Không được tự ý sửa số vé còn lại. Giá trị đúng sau khi lưu là "
                    + newRemaining + ".");
        }

        checkUniqueName(schedule.getScheduleId(), input.name(), ticketId);
        checkCapacity(schedule, input.total(), ticketId);

        ticket.setTicketName(input.name());
        ticket.setDesc(input.desc());
        ticket.setPrice(input.price());
        ticket.setTotalQuantity(input.total());
        ticket.setRemainingQuantity(newRemaining);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    // ======================= XÓA =======================

    @Override
    @Transactional
    public void deleteTicket(String ticketId) {
        Ticket ticket = lockTicketAndSchedule(ticketId);
        AccessRules.requireOwner(ticket.getSchedule().getEvent());
        // Vé đã nằm trong bất kỳ đơn hàng nào thì không được xóa (giữ lịch sử đơn)
        if (orderDetailRepository.existsByTicket_TicketId(ticketId)) {
            throw new BadRequestException("Hạng vé đã phát sinh đơn hàng, không thể xóa.");
        }
        ticketRepository.delete(ticket);
    }

    // ======================= ADMIN: thống kê và danh sách (chức năng cũ) =======================

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream().map(TicketResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(String ticketId) {
        return ticketRepository.findById(ticketId)
                .map(TicketResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé: " + ticketId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getAllTicketsWithDetails() {
        return ticketRepository.findAllTicketsWithDetails();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getTopEvents() {
        return ticketRepository.getTopEvents();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getRevenueByMonth() {
        return ticketRepository.getRevenueByMonth();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTicketCount() {
        return ticketRepository.count();
    }

    // ======================= HÀM PHỤ =======================

    /** Dữ liệu vé đã kiểm tra và chuẩn hóa (cắt khoảng trắng, desc null -> rỗng). */
    private record ValidatedTicket(String name, String desc, BigDecimal price, int total, Integer remaining) {
    }

    /** Kiểm tra giá trị đầu vào: tên, giá >= 0, tổng vé > 0, số còn lại (nếu gửi) >= 0 và <= tổng. */
    private ValidatedTicket validate(TicketRequestDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Dữ liệu hạng vé không được để trống.");
        }
        if (dto.getTicketName() == null || dto.getTicketName().isBlank()) {
            throw new BadRequestException("Tên hạng vé không được để trống.");
        }
        String name = dto.getTicketName().trim();
        if (name.length() > MAX_NAME) {
            throw new BadRequestException("Tên hạng vé tối đa " + MAX_NAME + " ký tự.");
        }
        String desc = dto.getDesc() == null ? "" : dto.getDesc().trim();
        if (desc.length() > MAX_DESC) {
            throw new BadRequestException("Mô tả hạng vé tối đa " + MAX_DESC + " ký tự.");
        }

        BigDecimal price = dto.getPrice();
        if (price == null || price.signum() < 0) {
            throw new BadRequestException("Giá vé phải lớn hơn hoặc bằng 0.");
        }
        if (price.stripTrailingZeros().scale() > 0 || price.compareTo(MAX_PRICE) > 0) {
            throw new BadRequestException("Giá vé phải là số nguyên VNĐ hợp lệ.");
        }
        price = price.setScale(0);

        Integer total = dto.getTotalQuantity();
        if (total == null || total <= 0) {
            throw new BadRequestException("Tổng số vé phải lớn hơn 0.");
        }
        Integer remaining = dto.getRemainingQuantity();
        if (remaining != null && (remaining < 0 || remaining > total)) {
            throw new BadRequestException("Số vé còn lại phải từ 0 đến tổng số vé.");
        }
        return new ValidatedTicket(name, desc, price, total, remaining);
    }

    /** Suất diễn phải thuộc sự kiện đã duyệt và chưa kết thúc mới được tạo/sửa vé. */
    private void requireUsableSchedule(EventSchedule schedule) {
        if (!AccessRules.EVENT_APPROVED.equals(schedule.getEvent().getStatus())) {
            throw new BadRequestException("Chỉ quản lý vé của sự kiện đã được duyệt (APPROVED).");
        }
        if (!schedule.getEndDatetime().isAfter(AppTime.now())) {
            throw new BadRequestException("Suất diễn đã kết thúc, không thể thay đổi hạng vé.");
        }
    }

    /** Tên hạng vé không được trùng trong cùng suất diễn (loại trừ chính vé đang sửa). */
    private void checkUniqueName(String scheduleId, String name, String excludeTicketId) {
        if (ticketRepository.existsDuplicateName(scheduleId, name, excludeTicketId)) {
            throw new BadRequestException("Tên hạng vé '" + name + "' đã tồn tại trong suất diễn này.");
        }
    }

    /** Tổng số vé của suất diễn (sau khi thêm/sửa) không được vượt sức chứa địa điểm. */
    private void checkCapacity(EventSchedule schedule, int newTotal, String excludeTicketId) {
        long others = ticketRepository.sumTotalQuantityExcluding(schedule.getScheduleId(), excludeTicketId);
        int capacity = schedule.getVenue().getCapacity();
        if (others + newTotal > capacity) {
            throw new BadRequestException("Tổng số vé của suất diễn (" + (others + newTotal)
                    + ") vượt sức chứa địa điểm (" + capacity + ").");
        }
    }

    /**
     * Khóa suất diễn rồi khóa vé (cùng thứ tự với createTicket) và trả về vé với dữ liệu mới nhất,
     * để việc đọc số vé đã bán không bị lệch khi có đơn đặt vé đang chạy.
     */
    private Ticket lockTicketAndSchedule(String ticketId) {
        String scheduleId = ticketRepository.findScheduleIdByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé: " + ticketId));
        scheduleRepository.findByIdForUpdate(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất diễn với ID: " + scheduleId));
        return ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé: " + ticketId));
    }
}
