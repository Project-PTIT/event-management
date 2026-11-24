package eventmanagement.repository;

import eventmanagement.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PeopleRepository extends JpaRepository<People, String> {
    Optional<People> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
