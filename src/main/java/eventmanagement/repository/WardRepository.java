package eventmanagement.repository;

import eventmanagement.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WardRepository extends JpaRepository<Ward, String> {

    /** Tất cả phường kèm tỉnh/thành (join fetch để không dính LazyInitializationException). */
    @Query("select w from Ward w join fetch w.province p order by p.provinceName, w.wardName")
    List<Ward> findAllWithProvince();

    /** Các phường thuộc một tỉnh/thành. */
    @Query("select w from Ward w join fetch w.province p where p.provinceId = :provinceId order by w.wardName")
    List<Ward> findByProvinceIdWithProvince(@Param("provinceId") String provinceId);

    @Query("select w from Ward w join fetch w.province p where w.wardId = :wardId")
    Optional<Ward> findByIdWithProvince(@Param("wardId") String wardId);
}
