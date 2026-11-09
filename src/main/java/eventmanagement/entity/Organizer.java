package eventmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organizer")
@PrimaryKeyJoinColumn(name = "organizer_id")
@Getter @Setter @NoArgsConstructor
public class Organizer extends People {

    @Column(name = "tax_code", nullable = false, unique = true, length = 255)
    private String taxCode;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;
}
