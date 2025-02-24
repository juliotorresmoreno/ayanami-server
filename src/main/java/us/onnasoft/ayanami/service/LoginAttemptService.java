package us.onnasoft.ayanami.service;

import us.onnasoft.ayanami.models.LoginAttempt;
import us.onnasoft.ayanami.repository.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoginAttemptService {

  private final LoginAttemptRepository loginAttemptRepository;

  @Autowired
  public LoginAttemptService(LoginAttemptRepository loginAttemptRepository) {
    this.loginAttemptRepository = loginAttemptRepository;
  }

  public void recordLoginAttempt(Long userId, String ipAddress, boolean success) {
    LoginAttempt loginAttempt = new LoginAttempt();
    loginAttempt.setUserId(userId);
    loginAttempt.setIpAddress(ipAddress);
    loginAttempt.setSuccess(success);
    loginAttempt.setTimestamp(LocalDateTime.now());
    
    loginAttemptRepository.save(loginAttempt);
  }

  public List<LoginAttempt> getLoginAttemptsByUserId(Long userId) {
    return loginAttemptRepository.findByUserId(userId);
  }

  public List<LoginAttempt> getLoginAttemptsByStatus(boolean success) {
    return loginAttemptRepository.findBySuccess(success);
  }
}
