package eventmanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "organizer")
@PrimaryKeyJoinColumn(name = "organizer_id")
@Getter
@Setter
@NoArgsConstructor
public class Organizer extends People {
    @Column(name = "tax_code", nullable = false, unique = true, length = 100)
    private String taxCode;

    @Column(name = "address", nullable = false, length = 200)
    private String address;

    @JsonIgnore
    @OneToMany(mappedBy = "organizer")
    private List<Event> events = new ArrayList<>();
}
