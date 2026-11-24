package eventmanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "`event`")
@Getter
@Setter
@NoArgsConstructor
public class Event {
    @Id
    @Column(name = "event_id", length = 20)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @Column(name = "title", nullable = false, length = 1000)
    private String title;

    @Column(name = "image", nullable = false, length = 1000)
    private String image;

    @Column(name = "`desc`", nullable = false, length = 1000)
    private String desc;

    @Column(name = "create_type", length = 100)
    private String createType;

    @Column(name = "`status`", nullable = false, length = 20)
    private String status = "PENDING";

    @JsonIgnore
    @OneToMany(mappedBy = "event")
    private List<EventSchedule> schedules = new ArrayList<>();
}
