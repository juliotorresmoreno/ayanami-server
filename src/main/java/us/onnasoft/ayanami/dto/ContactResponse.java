package us.onnasoft.ayanami.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContactResponse {
  private boolean success;
  private String message;
}
