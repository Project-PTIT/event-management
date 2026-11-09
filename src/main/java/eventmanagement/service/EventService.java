package eventmanagement.service;

import eventmanagement.dto.EventCreateRequest;
import eventmanagement.dto.EventResponse;
import eventmanagement.dto.EventUpdateRequest;
import eventmanagement.dto.PageResponse;
import eventmanagement.entity.Event;
import eventmanagement.entity.Organizer;
import eventmanagement.exception.BadRequestException;
import eventmanagement.exception.ForbiddenException;
import eventmanagement.exception.ResourceNotFoundException;
import eventmanagement.repository.EventRepository;
import eventmanagement.repository.OrganizerRepository;
import eventmanagement.util.CurrentUser;
import eventmanagement.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_ORGANIZER = "ROLE_ORGANIZER";

    private static final String PENDING = "PENDING";
    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";

    private static final Set<String> STATUSES = Set.of(PENDING, APPROVED, REJECTED);
    private static final Set<String> CREATE_TYPES = Set.of("WEEKLY", "MONTHLY", "YEARLY");
    private static final int MAX_TEXT = 1000; // title, image, desc đều VARCHAR(1000)

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;

    // ======================= CÔNG KHAI =======================

    @Transactional(readOnly = true)
    public PageResponse<EventResponse> searchPublic(String keyword, String createType, String wardId,
                                                    LocalDate fromDate, LocalDate toDate,
                                                    int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("page phải từ 0 trở lên và size trong khoảng 1 đến 100.");
        }
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new BadRequestException("toDate không được trước fromDate.");
        }
        String type = blankToNull(createType);
        if (type != null) {
            checkCreateType(type);
        }
        LocalDateTime fromTime = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime toTime = toDate == null ? null : toDate.plusDays(1).atStartOfDay();

        var result = eventRepository
                .searchApproved(blankToNull(keyword), type, blankToNull(wardId), fromTime, toTime,
                        PageRequest.of(page, size, Sort.by("eventId")))
                .map(EventResponse::from);
        return PageResponse.from(result);
    }

    /**
     * Người ngoài chỉ xem được sự kiện APPROVED. Chủ sự kiện và Admin xem được
     * mọi trạng thái (để form sửa của Organizer tải được sự kiện PENDING/REJECTED).
     */
    @Transactional(readOnly = true)
    public EventResponse getDetail(String eventId) {
        Event e = findOrThrow(eventId);
        if (!APPROVED.equals(e.getStatus()) && !canViewUnapproved(e)) {
            throw new ResourceNotFoundException("Không tìm thấy sự kiện " + eventId);
        }
        return EventResponse.from(e);
    }

    // ======================= ORGANIZER =======================

    @Transactional(readOnly = true)
    public List<EventResponse> listMine(String status) {
        requireRole(ROLE_ORGANIZER);
        String st = blankToNull(status);
        if (st != null) {
            checkStatus(st);
        }
        String me = CurrentUser.getId();
        List<Event> events = (st == null)
                ? eventRepository.findByOrganizer_Id(me)
                : eventRepository.findByOrganizer_IdAndStatus(me, st);
        return events.stream().map(EventResponse::from).toList();
    }

    @Transactional
    public EventResponse create(EventCreateRequest req) {
        requireRole(ROLE_ORGANIZER);
        Organizer organizer = organizerRepository.findById(CurrentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Chỉ nhà tổ chức mới được tạo sự kiện."));

        Event e = new Event();
        e.setEventId(nextEventId());
        e.setOrganizer(organizer);
        e.setStatus(PENDING);
        fill(e, req.title(), req.image(), req.desc(), req.createType());
        return EventResponse.from(eventRepository.save(e));
    }

    @Transactional
    public EventResponse update(String eventId, EventUpdateRequest req) {
        requireRole(ROLE_ORGANIZER);
        Event e = findOrThrow(eventId);
        if (!e.getOrganizer().getId().equals(CurrentUser.getId())) {
            throw new ForbiddenException("Bạn chỉ được sửa sự kiện của chính mình.");
        }
        fill(e, req.title(), req.image(), req.desc(), req.createType());
        if (REJECTED.equals(e.getStatus())) {
            e.setStatus(PENDING); // sửa xong gửi duyệt lại
        }
        return EventResponse.from(e); // entity đang managed, tự flush khi commit
    }

    // ======================= ADMIN =======================

    @Transactional(readOnly = true)
    public List<EventResponse> listForAdmin(String status) {
        requireRole(ROLE_ADMIN);
        String st = blankToNull(status);
        if (st != null) {
            checkStatus(st);
        }
        List<Event> events = (st == null) ? eventRepository.findAll() : eventRepository.findByStatus(st);
        return events.stream().map(EventResponse::from).toList();
    }

    @Transactional
    public EventResponse approve(String eventId) {
        return changeStatus(eventId, APPROVED);
    }

    @Transactional
    public EventResponse reject(String eventId) {
        return changeStatus(eventId, REJECTED);
    }

    private EventResponse changeStatus(String eventId, String newStatus) {
        requireRole(ROLE_ADMIN);
        Event e = findOrThrow(eventId);
        if (!PENDING.equals(e.getStatus())) {
            throw new BadRequestException("Chỉ duyệt hoặc từ chối được sự kiện đang PENDING.");
        }
        e.setStatus(newStatus);
        return EventResponse.from(e);
    }

    // ======================= HÀM PHỤ =======================

    private Event findOrThrow(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự kiện " + eventId));
    }

    /** IdGenerator đếm trong bộ nhớ nên phải bỏ qua các mã đã có trong DB. */
    private String nextEventId() {
        String id;
        do {
            id = IdGenerator.nextId("SK", 2);
        } while (eventRepository.existsById(id));
        return id;
    }

    private boolean canViewUnapproved(Event e) {
        return ROLE_ADMIN.equals(CurrentUser.getRole())
                || e.getOrganizer().getId().equals(CurrentUser.getId());
    }

    /** Kiểm tra và gán dữ liệu dùng chung cho tạo và sửa. */
    private void fill(Event e, String title, String image, String desc, String createType) {
        e.setTitle(requireText(title, "title"));
        e.setImage(requireText(image, "image"));
        e.setDesc(requireText(desc, "desc"));
        String type = blankToNull(createType);
        if (type != null) {
            checkCreateType(type);
        }
        e.setCreateType(type);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(field + " không được để trống.");
        }
        String v = value.trim();
        if (v.length() > MAX_TEXT) {
            throw new BadRequestException(field + " tối đa " + MAX_TEXT + " ký tự.");
        }
        return v;
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static void checkCreateType(String type) {
        if (!CREATE_TYPES.contains(type)) {
            throw new BadRequestException("createType chỉ nhận WEEKLY, MONTHLY, YEARLY hoặc để trống.");
        }
    }

    private static void checkStatus(String status) {
        if (!STATUSES.contains(status)) {
            throw new BadRequestException("status chỉ nhận PENDING, APPROVED hoặc REJECTED.");
        }
    }

    private static void requireRole(String role) {
        if (!role.equals(CurrentUser.getRole())) {
            throw new ForbiddenException("Bạn không có quyền thực hiện thao tác này.");
        }
    }
}
