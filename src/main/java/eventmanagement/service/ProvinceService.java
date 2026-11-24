package eventmanagement.service;

import eventmanagement.dto.ProvinceResponse;
import eventmanagement.exception.ResourceNotFoundException;
import eventmanagement.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Dữ liệu tỉnh/thành phố (công khai, chỉ đọc). */
@Service
@RequiredArgsConstructor
public class ProvinceService {

    private final ProvinceRepository provinceRepository;

    /** Tất cả tỉnh/thành, sắp theo tên. */
    @Transactional(readOnly = true)
    public List<ProvinceResponse> getAllProvinces() {
        return provinceRepository.findAllByOrderByProvinceNameAsc().stream()
                .map(ProvinceResponse::from)
                .toList();
    }

    /** Một tỉnh/thành theo mã; không có thì ném 404. */
    @Transactional(readOnly = true)
    public ProvinceResponse getProvinceById(String id) {
        return provinceRepository.findById(id)
                .map(ProvinceResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tỉnh/thành phố với ID: " + id));
    }
}
