package us.onnasoft.ayanami.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
  private String email;
  private String password;
}
