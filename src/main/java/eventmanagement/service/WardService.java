package eventmanagement.service;

import eventmanagement.dto.WardResponse;
import eventmanagement.entity.Ward;
import eventmanagement.exception.ResourceNotFoundException;
import eventmanagement.repository.ProvinceRepository;
import eventmanagement.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Dữ liệu phường/xã (công khai, chỉ đọc). Có @Transactional vì ward.province là LAZY và open-in-view = false. */
@Service
@RequiredArgsConstructor
public class WardService {

    private final WardRepository wardRepository;
    private final ProvinceRepository provinceRepository;

    /**
     * Danh sách phường. Nếu có provinceId thì chỉ trả phường của tỉnh/thành đó
     * (provinceId không tồn tại thì 404 để frontend biết lựa chọn sai).
     */
    @Transactional(readOnly = true)
    public List<WardResponse> getWards(String provinceId) {
        List<Ward> wards;
        if (provinceId == null || provinceId.isBlank()) {
            wards = wardRepository.findAllWithProvince();
        } else {
            String id = provinceId.trim();
            if (!provinceRepository.existsById(id)) {
                throw new ResourceNotFoundException("Không tìm thấy tỉnh/thành phố với ID: " + id);
            }
            wards = wardRepository.findByProvinceIdWithProvince(id);
        }
        return wards.stream().map(WardResponse::from).toList();
    }

    /** Chi tiết một phường. */
    @Transactional(readOnly = true)
    public WardResponse getWardById(String id) {
        Ward ward = wardRepository.findByIdWithProvince(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phường với ID: " + id));
        return WardResponse.from(ward);
    }
}
