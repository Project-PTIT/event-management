package eventmanagement.service;

import eventmanagement.dto.VenueResponse;
import eventmanagement.exception.ResourceNotFoundException;
import eventmanagement.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Dữ liệu địa điểm (công khai, chỉ đọc). Có @Transactional vì ward/province là LAZY và open-in-view = false. */
@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    /** Tất cả địa điểm kèm phường và tỉnh/thành. */
    @Transactional(readOnly = true)
    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAllWithWardAndProvince().stream().map(VenueResponse::from).toList();
    }

    /** Chi tiết một địa điểm. */
    @Transactional(readOnly = true)
    public VenueResponse getVenueById(String id) {
        return venueRepository.findByIdWithWardAndProvince(id)
                .map(VenueResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa điểm với ID: " + id));
    }
}
