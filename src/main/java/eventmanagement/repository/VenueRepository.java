package eventmanagement.repository;

import eventmanagement.entity.Venue;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VenueRepository extends JpaRepository<Venue, String> {

    /** Lấy toàn bộ địa điểm kèm phường và tỉnh trong một truy vấn (tránh lỗi LAZY và N+1). */
    @Query("select v from Venue v left join fetch v.ward w left join fetch w.province p " +
            "order by p.provinceName, w.wardName, v.venueName")
    List<Venue> findAllWithWardAndProvince();

    @Query("select v from Venue v left join fetch v.ward w left join fetch w.province p where v.venueId = :venueId")
    Optional<Venue> findByIdWithWardAndProvince(@Param("venueId") String venueId);

    /**
     * Khóa dòng địa điểm khi tạo/sửa suất diễn: các request cùng địa điểm xếp hàng nhau
     * nên kiểm tra trùng lịch không bị "lọt" khi hai request chạy đồng thời.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Venue v where v.venueId = :venueId")
    Optional<Venue> findByIdForUpdate(@Param("venueId") String venueId);
}
