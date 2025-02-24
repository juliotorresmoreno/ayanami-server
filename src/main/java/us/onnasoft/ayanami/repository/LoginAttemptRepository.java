package us.onnasoft.ayanami.repository;

import us.onnasoft.ayanami.models.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByUserId(Long userId);

    List<LoginAttempt> findBySuccess(boolean success);
}