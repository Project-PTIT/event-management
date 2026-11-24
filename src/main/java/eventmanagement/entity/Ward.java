package eventmanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ward")
@Getter
@Setter
@NoArgsConstructor
public class Ward {
    @Id
    @Column(name = "ward_id", length = 20)
    private String wardId;

    @Column(name = "ward_name", nullable = false, length = 100)
    private String wardName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

    @JsonIgnore
    @OneToMany(mappedBy = "ward")
    private List<Venue> venues = new ArrayList<>();
}
