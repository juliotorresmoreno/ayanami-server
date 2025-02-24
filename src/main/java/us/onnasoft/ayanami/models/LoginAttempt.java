package us.onnasoft.ayanami.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttempt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private LocalDateTime timestamp;

  @Column(nullable = false)
  private String ipAddress;

  @Column(nullable = false)
  private boolean success;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  public LoginAttempt(Long userId, LocalDateTime timestamp, String ipAddress, boolean success) {
    this.userId = userId;
    this.timestamp = timestamp;
    this.ipAddress = ipAddress;
    this.success = success;
  }
}
