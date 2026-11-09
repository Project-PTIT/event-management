package eventmanagement.dto;

import lombok.Data;

@Data
public class ScheduleArtistResponse {
    private ArtistResponse artist;
    private String role;
}