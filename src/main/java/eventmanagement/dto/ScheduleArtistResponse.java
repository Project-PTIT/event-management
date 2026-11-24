package eventmanagement.dto;

import eventmanagement.entity.ScheduleArtist;
import lombok.Data;

/** Nghệ sĩ tham gia một suất diễn. JSON phẳng: { artistId, artistName, image, role }. */
@Data
public class ScheduleArtistResponse {
    private String artistId;
    private String artistName;
    private String image;
    private String role;

    /** Gọi trong transaction vì scheduleArtist.artist là LAZY. */
    public static ScheduleArtistResponse from(ScheduleArtist sa) {
        ScheduleArtistResponse response = new ScheduleArtistResponse();
        response.setArtistId(sa.getArtist().getArtistId());
        response.setArtistName(sa.getArtist().getArtistName());
        response.setImage(sa.getArtist().getImage());
        response.setRole(sa.getRole());
        return response;
    }
}
