package eventmanagement.dto;

import lombok.Data;

@Data
public class VenueResponse {
    private String id;
    private String venueName;
    private String address;
    private Integer capacity;
    private WardResponse ward;
}