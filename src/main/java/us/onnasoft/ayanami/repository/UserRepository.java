package us.onnasoft.ayanami.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import us.onnasoft.ayanami.models.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
