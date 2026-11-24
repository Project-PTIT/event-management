package eventmanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "schedule_artist")
@Getter
@Setter
@NoArgsConstructor
public class ScheduleArtist {
    @EmbeddedId
    private ScheduleArtistId id = new ScheduleArtistId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("artistId")
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("scheduleId")
    @JoinColumn(name = "schedule_id", nullable = false)
    private EventSchedule schedule;

    @Column(name = "`role`", nullable = false, length = 100)
    private String role;

    public void setArtist(Artist artist) {
        this.artist = artist;
        if (artist != null) {
            if (this.id == null) this.id = new ScheduleArtistId();
            this.id.setArtistId(artist.getArtistId());
        }
    }

    public void setSchedule(EventSchedule schedule) {
        this.schedule = schedule;
        if (schedule != null) {
            if (this.id == null) this.id = new ScheduleArtistId();
            this.id.setScheduleId(schedule.getScheduleId());
        }
    }
}
