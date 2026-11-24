package eventmanagement.service;

import eventmanagement.dto.ScheduleArtistRequest;
import eventmanagement.dto.ScheduleArtistResponse;
import eventmanagement.dto.ScheduleRequest;
import eventmanagement.dto.ScheduleResponse;
import eventmanagement.dto.VenueResponse;
import eventmanagement.entity.Artist;
import eventmanagement.entity.Event;
import eventmanagement.entity.EventSchedule;
import eventmanagement.entity.ScheduleArtist;
import eventmanagement.entity.ScheduleArtistId;
import eventmanagement.entity.Venue;
import eventmanagement.exception.BadRequestException;
import eventmanagement.exception.ResourceNotFoundException;
import eventmanagement.repository.ArtistRepository;
import eventmanagement.repository.EventRepository;
import eventmanagement.repository.EventScheduleRepository;
import eventmanagement.repository.ScheduleArtistRepository;
import eventmanagement.repository.TicketRepository;
import eventmanagement.repository.VenueRepository;
import eventmanagement.util.AccessRules;
import eventmanagement.util.AppTime;
import eventmanagement.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Nghiệp vụ suất diễn (event_schedule) và gán nghệ sĩ.
 * Tất cả phương thức đều có @Transactional vì entity -> DTO đi qua nhiều quan hệ LAZY (open-in-view = false).
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private static final String DEFAULT_ARTIST_ROLE = "Nghệ sĩ biểu diễn";
    private static final int MAX_ROLE_LENGTH = 100; // schedule_artist.role VARCHAR(100)

    private final EventScheduleRepository scheduleRepository;
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final ArtistRepository artistRepository;
    private final ScheduleArtistRepository scheduleArtistRepository;
    private final TicketRepository ticketRepository;

    // ======================= XEM (CÔNG KHAI) =======================

    /** Danh sách suất diễn của một sự kiện. Sự kiện chưa duyệt chỉ chủ sự kiện và Admin xem được. */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByEventId(String eventId) {
        Event event = findViewableEvent(eventId);
        return scheduleRepository.findByEvent_EventIdOrderByStartDatetimeAsc(event.getEventId()).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Chi tiết một suất diễn (cùng quy tắc hiển thị như danh sách). */
    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleById(String scheduleId) {
        EventSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất diễn với ID: " + scheduleId));
        if (!AccessRules.canView(schedule.getEvent())) {
            throw new ResourceNotFoundException("Không tìm thấy suất diễn với ID: " + scheduleId);
        }
        return toResponse(schedule);
    }

    // ======================= ORGANIZER =======================

    /** Tạo suất diễn cho sự kiện của chính Organizer đang đăng nhập; sự kiện phải đã APPROVED. */
    @Transactional
    public ScheduleResponse createSchedule(String eventId, ScheduleRequest request) {
        validateTimes(request);
        if (request.getVenueId() == null || request.getVenueId().isBlank()) {
            throw new BadRequestException("venueId không được để trống.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự kiện " + eventId));
        AccessRules.requireOwner(event); // Organizer A không được tạo suất cho sự kiện của Organizer B
        if (!AccessRules.EVENT_APPROVED.equals(event.getStatus())) {
            throw new BadRequestException("Chỉ được tạo suất diễn cho sự kiện đã được duyệt (APPROVED).");
        }
        if (!request.getStartDatetime().isAfter(AppTime.now())) {
            throw new BadRequestException("Thời gian bắt đầu phải ở tương lai.");
        }

        // Khóa dòng địa điểm để hai request cùng địa điểm xếp hàng, kiểm tra trùng lịch không bị lọt
        Venue venue = venueRepository.findByIdForUpdate(request.getVenueId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa điểm."));

        if (scheduleRepository.existsOverlappingSchedule(
                venue.getVenueId(), request.getStartDatetime(), request.getEndDatetime())) {
            throw new BadRequestException("Địa điểm này đã có suất diễn khác trùng vào khoảng thời gian bạn chọn.");
        }

        EventSchedule schedule = new EventSchedule();
        schedule.setScheduleId(IdGenerator.nextId("SCH", 3)); // SCH001, SCH002... (không dùng UUID)
        schedule.setEvent(event);
        schedule.setVenue(venue);
        schedule.setStartDatetime(request.getStartDatetime());
        schedule.setEndDatetime(request.getEndDatetime());
        schedule.setStatus(calculateStatus(request.getStartDatetime(), request.getEndDatetime()));

        return toResponse(scheduleRepository.save(schedule));
    }

    /** Sửa giờ hoặc địa điểm của suất diễn thuộc sự kiện của chính Organizer. */
    @Transactional
    public ScheduleResponse updateSchedule(String scheduleId, ScheduleRequest request) {
        validateTimes(request);

        EventSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất diễn với ID: " + scheduleId));
        AccessRules.requireOwner(schedule.getEvent());

        LocalDateTime now = AppTime.now();
        if (!schedule.getEndDatetime().isAfter(now)) {
            throw new BadRequestException("Suất diễn đã kết thúc, không thể chỉnh sửa.");
        }
        // Chỉ bắt buộc giờ bắt đầu ở tương lai khi người dùng đổi giờ bắt đầu
        if (!request.getStartDatetime().equals(schedule.getStartDatetime())
                && !request.getStartDatetime().isAfter(now)) {
            throw new BadRequestException("Thời gian bắt đầu mới phải ở tương lai.");
        }

        // Địa điểm: không gửi venueId thì giữ nguyên địa điểm cũ
        String venueId = (request.getVenueId() == null || request.getVenueId().isBlank())
                ? schedule.getVenue().getVenueId()
                : request.getVenueId().trim();
        Venue venue = venueRepository.findByIdForUpdate(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa điểm."));

        // Loại trừ chính suất diễn đang sửa khỏi truy vấn trùng lịch
        if (scheduleRepository.existsOverlappingForUpdate(
                venue.getVenueId(), scheduleId, request.getStartDatetime(), request.getEndDatetime())) {
            throw new BadRequestException("Địa điểm này đã có suất diễn khác trùng vào khoảng thời gian bạn chọn.");
        }

        // Đổi sang địa điểm nhỏ hơn: tổng số vé đã tạo không được vượt sức chứa mới
        long totalTickets = ticketRepository.sumTotalQuantityExcluding(scheduleId, "");
        if (totalTickets > venue.getCapacity()) {
            throw new BadRequestException("Tổng số vé của suất diễn (" + totalTickets
                    + ") vượt sức chứa địa điểm mới (" + venue.getCapacity() + ").");
        }

        schedule.setVenue(venue);
        schedule.setStartDatetime(request.getStartDatetime());
        schedule.setEndDatetime(request.getEndDatetime());
        schedule.setStatus(calculateStatus(request.getStartDatetime(), request.getEndDatetime()));

        return toResponse(scheduleRepository.save(schedule));
    }

    /** Gán nghệ sĩ vào suất diễn thuộc sự kiện của chính Organizer. */
    @Transactional
    public ScheduleArtistResponse assignArtistToSchedule(String scheduleId, ScheduleArtistRequest request) {
        if (request == null || request.getArtistId() == null || request.getArtistId().isBlank()) {
            throw new BadRequestException("artistId không được để trống.");
        }

        EventSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất diễn với ID: " + scheduleId));
        AccessRules.requireOwner(schedule.getEvent()); // chỉ gán vào suất diễn thuộc sự kiện của mình
        if (!schedule.getEndDatetime().isAfter(AppTime.now())) {
            throw new BadRequestException("Suất diễn đã kết thúc, không thể gán nghệ sĩ.");
        }

        String artistId = request.getArtistId().trim();
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nghệ sĩ với ID: " + artistId));

        if (scheduleArtistRepository.existsById(new ScheduleArtistId(artistId, scheduleId))) {
            throw new BadRequestException("Nghệ sĩ này đã được gán vào suất diễn.");
        }

        String role = (request.getRole() == null || request.getRole().isBlank())
                ? DEFAULT_ARTIST_ROLE : request.getRole().trim();
        if (role.length() > MAX_ROLE_LENGTH) {
            throw new BadRequestException("Vai trò tối đa " + MAX_ROLE_LENGTH + " ký tự.");
        }

        ScheduleArtist scheduleArtist = new ScheduleArtist();
        scheduleArtist.setSchedule(schedule); // setter tự gán khóa ghép
        scheduleArtist.setArtist(artist);
        scheduleArtist.setRole(role);
        scheduleArtistRepository.save(scheduleArtist);

        return ScheduleArtistResponse.from(scheduleArtist);
    }

    // ======================= HÀM PHỤ =======================

    /** Tìm sự kiện và chỉ trả về nếu người gọi được phép xem (không lộ sự kiện chưa duyệt). */
    private Event findViewableEvent(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự kiện " + eventId));
        if (!AccessRules.canView(event)) {
            throw new ResourceNotFoundException("Không tìm thấy sự kiện " + eventId);
        }
        return event;
    }

    /** Kiểm tra request và thứ tự thời gian: bắt đầu và kết thúc bắt buộc có, kết thúc sau bắt đầu. */
    private void validateTimes(ScheduleRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu suất diễn không được để trống.");
        }
        if (request.getStartDatetime() == null || request.getEndDatetime() == null) {
            throw new BadRequestException("startDatetime và endDatetime không được để trống.");
        }
        if (!request.getEndDatetime().isAfter(request.getStartDatetime())) {
            throw new BadRequestException("Thời gian kết thúc bắt buộc phải sau thời gian bắt đầu.");
        }
    }

    /** Entity -> DTO. Gọi trong transaction vì venue, ward, province, scheduleArtists, artist đều LAZY. */
    private ScheduleResponse toResponse(EventSchedule schedule) {
        ScheduleResponse response = new ScheduleResponse();
        response.setScheduleId(schedule.getScheduleId());
        response.setEventId(schedule.getEvent().getEventId());
        response.setStartDatetime(schedule.getStartDatetime());
        response.setEndDatetime(schedule.getEndDatetime());
        response.setStatus(calculateStatus(schedule.getStartDatetime(), schedule.getEndDatetime()));
        response.setVenue(VenueResponse.from(schedule.getVenue()));
        response.setArtists(schedule.getScheduleArtists().stream().map(ScheduleArtistResponse::from).toList());
        return response;
    }

    /**
     * Trạng thái theo thời gian thực, đúng các giá trị CHECK của bảng event_schedule:
     * Đã kết thúc / Đang diễn ra / Sắp diễn ra (trong 30 ngày) / Đang mở bán (31-90 ngày) / Mở đặt vé sớm.
     */
    private String calculateStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = AppTime.now();
        if (now.isAfter(end)) {
            return "Đã kết thúc";
        }
        if (now.isAfter(start)) {
            return "Đang diễn ra";
        }
        long daysToStart = ChronoUnit.DAYS.between(now, start);
        if (daysToStart <= 30) {
            return "Sắp diễn ra";
        }
        if (daysToStart <= 90) {
            return "Đang mở bán";
        }
        return "Mở đặt vé sớm";
    }
}
