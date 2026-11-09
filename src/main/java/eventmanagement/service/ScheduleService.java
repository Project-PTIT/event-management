package eventmanagement.service;

import eventmanagement.dto.*;
import eventmanagement.entity.EventSchedule;
import eventmanagement.entity.ScheduleArtist;
import eventmanagement.repository.EventScheduleRepository;
import eventmanagement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final EventScheduleRepository scheduleRepository;
    private final eventmanagement.repository.EventRepository eventRepository;
    private final eventmanagement.repository.VenueRepository venueRepository;
    private final eventmanagement.repository.ArtistRepository artistRepository;
    private final eventmanagement.repository.ScheduleArtistRepository scheduleArtistRepository;

    // HÀM MỚI: TẠO SUẤT DIỄN
    public ScheduleResponse createSchedule(String eventId, ScheduleRequest request) {
        // 1. Kiểm tra logic thời gian cơ bản
        if (request.getStartDatetime().isAfter(request.getEndDatetime()) ||
                request.getStartDatetime().isEqual(request.getEndDatetime())) {
            throw new IllegalArgumentException("Thời gian kết thúc bắt buộc phải sau thời gian bắt đầu.");
        }

        // 2. Kiểm tra Sự kiện (Event) có tồn tại và đã được duyệt (APPROVED) chưa
        eventmanagement.entity.Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự kiện."));

        // Giả định trường status của Event là String, nếu nhóm dùng Enum thì bạn đổi lại cho khớp nhé
        if (!"APPROVED".equals(event.getStatus())) {
            throw new IllegalArgumentException("Chỉ được tạo suất diễn cho sự kiện đã được duyệt (APPROVED).");
        }

        // 3. Kiểm tra Địa điểm (Venue) có tồn tại không
        eventmanagement.entity.Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa điểm."));

        // 4. KIỂM TRA TRÙNG LỊCH (Thuật toán Overlap)
        boolean isOverlap = scheduleRepository.existsOverlappingSchedule(
                request.getVenueId(), request.getStartDatetime(), request.getEndDatetime()
        );
        if (isOverlap) {
            throw new IllegalArgumentException("Thất bại! Địa điểm này đã có suất diễn khác trùng vào khoảng thời gian bạn chọn.");
        }

        // 5. Khởi tạo và Lưu xuống Database
        EventSchedule newSchedule = new EventSchedule();
        // Tùy vào cách nhóm bạn set ID (tự sinh hay nhập tay), giả sử tự sinh UUID:
        newSchedule.setScheduleId(java.util.UUID.randomUUID().toString().substring(0, 20));

        newSchedule.setEvent(event);
        newSchedule.setVenue(venue);
        newSchedule.setStartDatetime(request.getStartDatetime());
        newSchedule.setEndDatetime(request.getEndDatetime());
        String calculatedStatus = calculateStatus(request.getStartDatetime(), request.getEndDatetime());
        System.out.println("--> GIA TRI STATUS CHU BI LUU: [" + calculatedStatus + "]"); // THÊM DÒNG NÀY ĐỂ DEBUG

        newSchedule.setStatus(calculatedStatus);
        scheduleRepository.save(newSchedule);

        return mapToResponse(newSchedule);
    }
    // Lấy danh sách suất diễn của một sự kiện
    public List<ScheduleResponse> getSchedulesByEventId(String eventId) {
        // Gọi hàm findByEvent_EventId đã định nghĩa trong EventScheduleRepository
        List<EventSchedule> schedules = scheduleRepository.findByEvent_EventId(eventId);

        // Chuyển đổi toàn bộ danh sách Entity sang DTO (bao gồm cả việc tính toán trạng thái động)
        return schedules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Lấy chi tiết 1 suất diễn
    public ScheduleResponse getScheduleById(String id) {
        EventSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất diễn với ID: " + id));
        return mapToResponse(schedule);
    }

    // Hàm mapping từ Entity sang DTO (Chuẩn OOP)
    private ScheduleResponse mapToResponse(EventSchedule schedule) {
        ScheduleResponse response = new ScheduleResponse();

        response.setId(schedule.getScheduleId()); // Khớp với entity
        response.setStartDatetime(schedule.getStartDatetime());
        response.setEndDatetime(schedule.getEndDatetime());

        // Gọi hàm tính trạng thái động
        response.setStatus(calculateStatus(schedule.getStartDatetime(), schedule.getEndDatetime()));

        // Map thông tin Địa điểm (Venue)
        if (schedule.getVenue() != null) {
            VenueResponse venueResponse = new VenueResponse();
            venueResponse.setId(schedule.getVenue().getVenueId());
            venueResponse.setVenueName(schedule.getVenue().getVenueName());
            venueResponse.setAddress(schedule.getVenue().getAddress());
            response.setVenue(venueResponse);
        }

        // Map danh sách nghệ sĩ
        if (schedule.getScheduleArtists() != null) {
            List<ScheduleArtistResponse> artistResponses = schedule.getScheduleArtists().stream()
                    .map(sa -> {
                        ScheduleArtistResponse saRes = new ScheduleArtistResponse();
                        saRes.setRole(sa.getRole()); // Giả định ScheduleArtist entity có trường role

                        if (sa.getArtist() != null) {
                            ArtistResponse aRes = new ArtistResponse();
                            aRes.setId(sa.getArtist().getArtistId());
                            aRes.setArtistName(sa.getArtist().getArtistName());
                            aRes.setImage(sa.getArtist().getImage());
                            saRes.setArtist(aRes);
                        }
                        return saRes;
                    }).collect(Collectors.toList());
            response.setArtists(artistResponses);
        }

        return response;
    }

    // Hàm tính toán trạng thái động theo thời gian thực
    private String calculateStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(end)) return "Đã kết thúc";
        if (now.isAfter(start) && now.isBefore(end)) return "Đang diễn ra";

        long daysToStart = ChronoUnit.DAYS.between(now, start);

        if (daysToStart <= 30) {
            return "Sắp diễn ra";
        } else if (daysToStart <= 90) {
            return "Đang mở bán";
        } else {
            return "Mở đặt vé sớm";
        }
    }
    // API: Gán nghệ sĩ vào suất diễn (Dành cho Organizer)
    public ScheduleArtistResponse assignArtistToSchedule(String scheduleId, ScheduleArtistRequest request) {
        // 1. Kiểm tra Suất diễn có tồn tại không
        EventSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất diễn với ID: " + scheduleId));

        // 2. Kiểm tra Nghệ sĩ có tồn tại không
        eventmanagement.entity.Artist artist = artistRepository.findById(request.getArtistId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nghệ sĩ với ID: " + request.getArtistId()));

        // 3. Khởi tạo đối tượng bảng trung gian
        ScheduleArtist scheduleArtist = new ScheduleArtist();

        // Nhờ code ở Entity đã cấu hình sẵn nên khi set Object nó tự động set luôn Composite Key (id)[cite: 6]
        scheduleArtist.setSchedule(schedule);
        scheduleArtist.setArtist(artist);
        scheduleArtist.setRole(request.getRole());

        // 4. Lưu xuống Database
        scheduleArtistRepository.save(scheduleArtist);

        // 5. Trả về kết quả Response chuẩn OOP
        ScheduleArtistResponse response = new ScheduleArtistResponse();
        response.setRole(scheduleArtist.getRole());

        ArtistResponse artistResponse = new ArtistResponse();
        artistResponse.setId(artist.getArtistId());
        artistResponse.setArtistName(artist.getArtistName());
        artistResponse.setImage(artist.getImage());

        response.setArtist(artistResponse);

        return response;
    }
    // API: Cập nhật suất diễn (Sửa giờ, đổi địa điểm)
    public ScheduleResponse updateSchedule(String scheduleId, ScheduleRequest request) {
        // 1. Tìm suất diễn hiện tại
        EventSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất diễn với ID: " + scheduleId));

        // 2. Kiểm tra logic thời gian cơ bản
        if (request.getStartDatetime().isAfter(request.getEndDatetime()) ||
                request.getStartDatetime().isEqual(request.getEndDatetime())) {
            throw new IllegalArgumentException("Thời gian kết thúc bắt buộc phải sau thời gian bắt đầu.");
        }

        // 3. Cập nhật địa điểm mới (nếu có yêu cầu thay đổi)
        if (request.getVenueId() != null && !request.getVenueId().equals(schedule.getVenue().getVenueId())) {
            eventmanagement.entity.Venue venue = venueRepository.findById(request.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa điểm."));
            schedule.setVenue(venue);
        }

        // 4. Kiểm tra trùng lịch (Bỏ qua chính suất diễn này)
        boolean isOverlap = scheduleRepository.existsOverlappingForUpdate(
                schedule.getVenue().getVenueId(), scheduleId, request.getStartDatetime(), request.getEndDatetime()
        );

        if (isOverlap) {
            throw new IllegalArgumentException("Thất bại! Địa điểm này đã có lịch khác trùng vào thời gian bạn vừa cập nhật.");
        }

        // 5. Cập nhật dữ liệu thời gian mới và lưu vào DB
        schedule.setStartDatetime(request.getStartDatetime());
        schedule.setEndDatetime(request.getEndDatetime());

        scheduleRepository.save(schedule);

        // Trả về DTO mới (hàm mapToResponse sẽ tự động gọi tính lại trạng thái status hiện tại)
        return mapToResponse(schedule);
    }
}