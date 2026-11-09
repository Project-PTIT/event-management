package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.WardResponse;
import eventmanagement.service.WardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;

    // GET /api/wards - Lấy danh sách phường[cite: 32]
    @GetMapping
    public ApiResponse<List<WardResponse>> getAllWards() {
        List<WardResponse> data = wardService.getAllWards();
        return ApiResponse.success("Lấy danh sách phường thành công", data);
    }

    // GET /api/wards/{id} - Lấy chi tiết một phường
    @GetMapping("/{id}")
    public ApiResponse<WardResponse> getWardById(@PathVariable String id) {
        WardResponse data = wardService.getWardById(id);
        return ApiResponse.success("Lấy chi tiết phường thành công", data);
    }
}