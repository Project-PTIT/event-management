package eventmanagement.service;

import eventmanagement.dto.ArtistResponse;
import eventmanagement.entity.Artist;
import eventmanagement.repository.ArtistRepository;
import eventmanagement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    // API lấy danh sách nghệ sĩ
    public List<ArtistResponse> getAllArtists() {
        List<Artist> artists = artistRepository.findAll();

        // Dùng Stream API của Java để duyệt và chuyển đổi từng Entity sang DTO
        return artists.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // API lấy chi tiết 1 nghệ sĩ
    public ArtistResponse getArtistById(String id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nghệ sĩ với ID: " + id));
        return mapToResponse(artist);
    }

    // Hàm phụ trợ map từ Entity sang DTO

    private ArtistResponse mapToResponse(Artist artist) {
        ArtistResponse response = new ArtistResponse();
        response.setId(artist.getArtistId());
        response.setArtistName(artist.getArtistName());
        response.setImage(artist.getImage());
        return response;
    }
}