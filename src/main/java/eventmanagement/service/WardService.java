package eventmanagement.service;

import eventmanagement.dto.ProvinceResponse;
import eventmanagement.dto.WardResponse;
import eventmanagement.entity.Ward;
import eventmanagement.repository.WardRepository;
import eventmanagement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WardService {

    private final WardRepository wardRepository;

    public List<WardResponse> getAllWards() {
        List<Ward> wards = wardRepository.findAll();
        return wards.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public WardResponse getWardById(String id) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phường với ID: " + id));
        return mapToResponse(ward);
    }

    private WardResponse mapToResponse(Ward ward) {
        WardResponse response = new WardResponse();
        response.setId(ward.getWardId()); // Khớp với wardId trong Entity Ward
        response.setWardName(ward.getWardName());

        // Lồng đối tượng Province vào theo chuẩn OOP
        if (ward.getProvince() != null) {
            ProvinceResponse provinceResponse = new ProvinceResponse();
            // Giả định Province entity có getProvinceId() hoặc getId() và getProvinceName()
            provinceResponse.setId(ward.getProvince().getProvinceId());
            provinceResponse.setProvinceName(ward.getProvince().getProvinceName());
            response.setProvince(provinceResponse);
        }

        return response;
    }
}