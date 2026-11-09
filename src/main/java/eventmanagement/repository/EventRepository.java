package eventmanagement.repository;

import eventmanagement.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, String> {

    /**
     * Tìm sự kiện APPROVED. Tham số nào null thì bỏ qua điều kiện đó.
     * wardId / fromTime / toTime lọc qua suất diễn: có ít nhất một suất
     * thỏa điều kiện (suất diễn -> địa điểm -> phường, ngày bắt đầu trong
     * khoảng [fromTime, toTime)).
     */
    @Query("""
            select e from Event e
            where e.status = 'APPROVED'
              and (:keyword is null or lower(e.title) like lower(concat('%', :keyword, '%')))
              and (:createType is null or e.createType = :createType)
              and ((:wardId is null and :fromTime is null and :toTime is null)
                   or exists (select 1 from EventSchedule s
                              where s.event = e
                                and (:wardId is null or s.venue.ward.wardId = :wardId)
                                and (:fromTime is null or s.startDatetime >= :fromTime)
                                and (:toTime is null or s.startDatetime < :toTime)))
            """)
    Page<Event> searchApproved(@Param("keyword") String keyword,
                               @Param("createType") String createType,
                               @Param("wardId") String wardId,
                               @Param("fromTime") LocalDateTime fromTime,
                               @Param("toTime") LocalDateTime toTime,
                               Pageable pageable);

    List<Event> findByOrganizer_Id(String organizerId);

    List<Event> findByOrganizer_IdAndStatus(String organizerId, String status);

    List<Event> findByStatus(String status);
}
