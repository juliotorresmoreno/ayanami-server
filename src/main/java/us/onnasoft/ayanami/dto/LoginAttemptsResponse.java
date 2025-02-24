package us.onnasoft.ayanami.dto;

import us.onnasoft.ayanami.models.LoginAttempt;

import java.util.List;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginAttemptsResponse {
  private List<LoginAttempt> loginAttempts;
}
