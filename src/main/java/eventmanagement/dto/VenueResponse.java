package eventmanagement.dto;

import eventmanagement.entity.Venue;
import lombok.Data;

/**
 * Địa điểm tổ chức kèm phường và tỉnh/thành.
 * JSON phẳng: { venueId, venueName, address, capacity, wardId, wardName, provinceId, provinceName }.
 */
@Data
public class VenueResponse {
    private String venueId;
    private String venueName;
    private String address;
    private Integer capacity;
    private String wardId;
    private String wardName;
    private String provinceId;
    private String provinceName;

    /** Gọi trong transaction vì venue.ward và ward.province là LAZY. */
    public static VenueResponse from(Venue venue) {
        VenueResponse response = new VenueResponse();
        response.setVenueId(venue.getVenueId());
        response.setVenueName(venue.getVenueName());
        response.setAddress(venue.getAddress());
        response.setCapacity(venue.getCapacity());
        // Phường/tỉnh có thể thiếu nếu dữ liệu chưa đầy đủ: vẫn trả địa điểm, các trường này để trống
        if (venue.getWard() != null) {
            response.setWardId(venue.getWard().getWardId());
            response.setWardName(venue.getWard().getWardName());
            if (venue.getWard().getProvince() != null) {
                response.setProvinceId(venue.getWard().getProvince().getProvinceId());
                response.setProvinceName(venue.getWard().getProvince().getProvinceName());
            }
        }
        return response;
    }
}
