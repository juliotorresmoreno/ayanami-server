package us.onnasoft.ayanami.models;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import us.onnasoft.ayanami.models.enums.Gender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Boolean active = true;

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
    @Temporal(TemporalType.DATE)
    private Date birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void setPassword(String rawPassword) {
        this.password = passwordEncoder.encode(rawPassword);
    }

    /**
     * Compara la contraseña en texto plano con la almacenada en la base de datos.
     * 
     * @param rawPassword La contraseña ingresada por el usuario.
     * @return true si la contraseña coincide, false si no.
     */
    public boolean isPasswordValid(String rawPassword) {
        return passwordEncoder.matches(rawPassword, this.password);
    }

    public enum Role {
        USER, ADMIN, MODERATOR
    }
}
