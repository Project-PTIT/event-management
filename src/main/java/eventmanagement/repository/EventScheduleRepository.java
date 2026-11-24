package eventmanagement.repository;

import eventmanagement.entity.EventSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventScheduleRepository extends JpaRepository<EventSchedule, String> {

    /** Các suất diễn của một sự kiện, sắp theo giờ bắt đầu. */
    List<EventSchedule> findByEvent_EventIdOrderByStartDatetimeAsc(String eventId);

    /** Giữ lại hàm cũ để không ảnh hưởng nơi khác. */
    List<EventSchedule> findByEvent_EventId(String eventId);

    /** Khóa dòng suất diễn khi tạo/sửa hạng vé để kiểm tra sức chứa không bị vượt do hai request đồng thời. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from EventSchedule s where s.scheduleId = :scheduleId")
    Optional<EventSchedule> findByIdForUpdate(@Param("scheduleId") String scheduleId);

    /** Kiểm tra trùng lịch tại cùng địa điểm khi TẠO suất diễn mới. */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM EventSchedule s " +
            "WHERE s.venue.venueId = :venueId " +
            "AND s.startDatetime < :end " +
            "AND s.endDatetime > :start")
    boolean existsOverlappingSchedule(@Param("venueId") String venueId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    /** Kiểm tra trùng lịch khi SỬA: loại trừ chính suất diễn đang sửa (so sánh bằng <>, đúng chuẩn SQL). */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM EventSchedule s " +
            "WHERE s.venue.venueId = :venueId " +
            "AND s.scheduleId <> :scheduleId " +
            "AND s.startDatetime < :end " +
            "AND s.endDatetime > :start")
    boolean existsOverlappingForUpdate(@Param("venueId") String venueId,
                                       @Param("scheduleId") String scheduleId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}
