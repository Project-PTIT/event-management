package eventmanagement.dto;

import eventmanagement.entity.Ward;
import lombok.Data;

/** Phường/xã kèm tỉnh/thành. JSON phẳng: { wardId, wardName, provinceId, provinceName }. */
@Data
public class WardResponse {
    private String wardId;
    private String wardName;
    private String provinceId;
    private String provinceName;

    /** Gọi trong transaction hoặc sau join fetch vì ward.province là LAZY. */
    public static WardResponse from(Ward ward) {
        WardResponse response = new WardResponse();
        response.setWardId(ward.getWardId());
        response.setWardName(ward.getWardName());
        response.setProvinceId(ward.getProvince().getProvinceId());
        response.setProvinceName(ward.getProvince().getProvinceName());
        return response;
    }
}
