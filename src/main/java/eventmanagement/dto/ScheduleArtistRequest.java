package eventmanagement.dto;

import lombok.Data;

/** Dữ liệu gán nghệ sĩ vào suất diễn. JSON: { artistId, role }. */
@Data
public class ScheduleArtistRequest {
    private String artistId;
    private String role;
}
