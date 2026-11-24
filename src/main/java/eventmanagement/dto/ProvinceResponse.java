package eventmanagement.dto;

import eventmanagement.entity.Province;
import lombok.Data;

/** Tỉnh/thành phố. JSON: { provinceId, provinceName } (camelCase, khớp frontend). */
@Data
public class ProvinceResponse {
    /** Mã tỉnh/thành (biển số xe chính, ví dụ 29 = Hà Nội). */
    private String provinceId;
    /** Tên đầy đủ, ví dụ "Thành phố Hà Nội". */
    private String provinceName;

    /** Đổi entity sang DTO. */
    public static ProvinceResponse from(Province province) {
        ProvinceResponse response = new ProvinceResponse();
        response.setProvinceId(province.getProvinceId());
        response.setProvinceName(province.getProvinceName());
        return response;
    }
}
