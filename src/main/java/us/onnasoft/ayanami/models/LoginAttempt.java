package us.onnasoft.ayanami.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
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

  public LoginAttempt() {
  }

  public LoginAttempt(Long userId, LocalDateTime timestamp, String ipAddress, boolean success) {
    this.userId = userId;
    this.timestamp = timestamp;
    this.ipAddress = ipAddress;
    this.success = success;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }
}