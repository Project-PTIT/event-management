package eventmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "`event`")
@Getter @Setter @NoArgsConstructor
public class Event {

    @Id
    @Column(name = "event_id", length = 20)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "image", nullable = false, columnDefinition = "TEXT")
    private String image;

    @Column(name = "`desc`", nullable = false, columnDefinition = "TEXT")
    private String desc;

    @Column(name = "create_type", length = 255)
    private String createType;
}
