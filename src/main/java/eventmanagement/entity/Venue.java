package eventmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venue")
@Getter @Setter @NoArgsConstructor
public class Venue {

    @Id
    @Column(name = "venue_id", length = 20)
    private String venueId;

    @Column(name = "venue_name", nullable = false, columnDefinition = "TEXT")
    private String venueName;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @Column(name = "city", length = 255)
    private String city = "Hà Nội";
}
