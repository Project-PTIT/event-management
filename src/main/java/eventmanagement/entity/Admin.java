package eventmanagement.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "`admin`")
@PrimaryKeyJoinColumn(name = "admin_id")
@Getter
@Setter
@NoArgsConstructor
public class Admin extends People {
}
