package eventmanagement.dto;

import lombok.Data;

@Data
public class WardResponse {
    private String id;
    private String wardName;
    private ProvinceResponse province;
}