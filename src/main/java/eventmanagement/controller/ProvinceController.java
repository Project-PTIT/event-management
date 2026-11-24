package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.dto.ProvinceResponse;
import eventmanagement.service.ProvinceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API công khai (chỉ GET) về tỉnh/thành phố. */
@RestController
@RequestMapping("/api/provinces")
@RequiredArgsConstructor
public class ProvinceController {

    private final ProvinceService provinceService;

    /** GET /api/provinces - danh sách tỉnh/thành. */
    @GetMapping
    public ApiResponse<List<ProvinceResponse>> getAllProvinces() {
        return ApiResponse.success("Lấy danh sách tỉnh/thành phố thành công", provinceService.getAllProvinces());
    }

    /** GET /api/provinces/{id} - chi tiết một tỉnh/thành. */
    @GetMapping("/{id}")
    public ApiResponse<ProvinceResponse> getProvinceById(@PathVariable String id) {
        return ApiResponse.success("Lấy chi tiết tỉnh/thành phố thành công", provinceService.getProvinceById(id));
    }
}
