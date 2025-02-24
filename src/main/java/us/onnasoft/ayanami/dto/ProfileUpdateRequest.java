package us.onnasoft.ayanami.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import us.onnasoft.ayanami.models.enums.Gender;

import java.util.Date;

@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class ProfileUpdateRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @Size(min = 10, max = 15, message = "Phone must be between 10 and 15 characters")
    private String phone;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;

    @Size(max = 100, message = "Location must be at most 100 characters")
    private String location;

    @Size(max = 200, message = "Website must be at most 200 characters")
    @Pattern(regexp = "^(http|https)://.*", message = "Website must be a valid URL")
    private String website;

    @Past(message = "Birth date must be in the past")
    private Date birthDate;

    private Gender gender;
}