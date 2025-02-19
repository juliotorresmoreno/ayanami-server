package us.onnasoft.ayanami.dto;

import lombok.Data;

@Data
public class PasswordResetConfirm {
    private String token;
    private String newPassword;
}
