package eventmanagement.dto;

import eventmanagement.entity.Artist;
import lombok.Data;

/** Nghệ sĩ. JSON: { artistId, artistName, nationality, image, bio }. */
@Data
public class ArtistResponse {
    private String artistId;
    private String artistName;
    private String nationality;
    private String image;
    private String bio;

    public static ArtistResponse from(Artist artist) {
        ArtistResponse response = new ArtistResponse();
        response.setArtistId(artist.getArtistId());
        response.setArtistName(artist.getArtistName());
        response.setNationality(artist.getNationality());
        response.setImage(artist.getImage());
        response.setBio(artist.getBio());
        return response;
    }
}
