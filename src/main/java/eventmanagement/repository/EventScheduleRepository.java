package eventmanagement.repository;

import eventmanagement.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventScheduleRepository extends JpaRepository<EventSchedule, String> {

    // Hàm nguyên bản của bạn
    List<EventSchedule> findByEvent_EventId(String eventId);

    // BỔ SUNG: Truy vấn kiểm tra trùng lịch địa điểm (Overlap Check)
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM EventSchedule s " +
            "WHERE s.venue.venueId = :venueId " +
            "AND s.startDatetime < :end " +
            "AND s.endDatetime > :start")
    boolean existsOverlappingSchedule(@Param("venueId") String venueId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    // Truy vấn kiểm tra trùng lịch (loại trừ chính suất diễn đang sửa qua scheduleId)
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM EventSchedule s " +
            "WHERE s.venue.venueId = :venueId " +
            "AND s.scheduleId != :scheduleId " +
            "AND s.startDatetime < :end " +
            "AND s.endDatetime > :start")
    boolean existsOverlappingForUpdate(@Param("venueId") String venueId,
                                       @Param("scheduleId") String scheduleId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}
