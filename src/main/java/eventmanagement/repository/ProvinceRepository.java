package eventmanagement.repository;

import eventmanagement.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProvinceRepository extends JpaRepository<Province, String> {

    /** Danh sách tỉnh/thành sắp theo tên để hiển thị trong ô chọn. */
    List<Province> findAllByOrderByProvinceNameAsc();
}
