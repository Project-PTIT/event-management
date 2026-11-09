package eventmanagement.controller;

import eventmanagement.dto.ApiResponse; // File chuẩn của nhóm
import eventmanagement.dto.ArtistResponse;
import eventmanagement.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artists") // Đúng quy ước đường dẫn API của dự án
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    // Tương ứng với GET /api/artists
    @GetMapping
    public ApiResponse<List<ArtistResponse>> getAllArtists() {
        List<ArtistResponse> data = artistService.getAllArtists();
        return ApiResponse.success("Lấy danh sách nghệ sĩ thành công", data);
    }

    // Tương ứng với GET /api/artists/{id}
    @GetMapping("/{id}")
    public ApiResponse<ArtistResponse> getArtistById(@PathVariable String id) {
        ArtistResponse data = artistService.getArtistById(id);
        return ApiResponse.success("Lấy chi tiết nghệ sĩ thành công", data);
    }
}