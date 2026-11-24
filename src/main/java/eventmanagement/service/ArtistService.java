package eventmanagement.service;

import eventmanagement.dto.ArtistResponse;
import eventmanagement.entity.Artist;
import eventmanagement.exception.ResourceNotFoundException;
import eventmanagement.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Dữ liệu nghệ sĩ (công khai, chỉ đọc). */
@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    /** Danh sách tất cả nghệ sĩ. */
    @Transactional(readOnly = true)
    public List<ArtistResponse> getAllArtists() {
        return artistRepository.findAll().stream().map(ArtistResponse::from).toList();
    }

    /** Chi tiết một nghệ sĩ; không có thì ném 404. */
    @Transactional(readOnly = true)
    public ArtistResponse getArtistById(String id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nghệ sĩ với ID: " + id));
        return ArtistResponse.from(artist);
    }
}
