package eventmanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "event_schedule")
@Getter
@Setter
@NoArgsConstructor
public class EventSchedule {
    @Id
    @Column(name = "schedule_id", length = 20)
    private String scheduleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @JsonIgnore
    @OneToMany(mappedBy = "schedule")
    private List<ScheduleArtist> scheduleArtists = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "schedule")
    private List<Ticket> tickets = new ArrayList<>();
}
