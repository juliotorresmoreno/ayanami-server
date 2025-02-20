package us.onnasoft.ayanami.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import us.onnasoft.ayanami.models.User.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
  private String name;
  private String email;
  private String username;
  private Role role;
  private Boolean active = true;
}
