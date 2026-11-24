package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.VenueResponse;
import eventmanagement.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    // GET /api/venues - Lấy danh sách địa điểm
    @GetMapping
    public ApiResponse<List<VenueResponse>> getAllVenues() {
        List<VenueResponse> data = venueService.getAllVenues();
        return ApiResponse.success("Lấy danh sách địa điểm thành công", data);
    }

    // GET /api/venues/{id} - Lấy chi tiết địa điểm[cite: 17]
    @GetMapping("/{id}")
    public ApiResponse<VenueResponse> getVenueById(@PathVariable String id) {
        VenueResponse data = venueService.getVenueById(id);
        return ApiResponse.success("Lấy chi tiết địa điểm thành công", data);
    }
}