package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.WardResponse;
import eventmanagement.service.WardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API công khai (chỉ GET) về phường/xã. */
@RestController
@RequestMapping("/api/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;

    /** GET /api/wards hoặc GET /api/wards?provinceId=29 - danh sách phường (lọc theo tỉnh nếu có). */
    @GetMapping
    public ApiResponse<List<WardResponse>> getWards(@RequestParam(required = false) String provinceId) {
        return ApiResponse.success("Lấy danh sách phường thành công", wardService.getWards(provinceId));
    }

    /** GET /api/wards/{id} - chi tiết một phường. */
    @GetMapping("/{id}")
    public ApiResponse<WardResponse> getWardById(@PathVariable String id) {
        return ApiResponse.success("Lấy chi tiết phường thành công", wardService.getWardById(id));
    }
}
