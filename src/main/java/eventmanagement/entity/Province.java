package eventmanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "province")
@Getter
@Setter
@NoArgsConstructor
public class Province {
    @Id
    @Column(name = "province_id", length = 20)
    private String provinceId;

    @Column(name = "province_name", nullable = false, unique = true, length = 100)
    private String provinceName;

    @JsonIgnore
    @OneToMany(mappedBy = "province")
    private List<Ward> wards = new ArrayList<>();
}
