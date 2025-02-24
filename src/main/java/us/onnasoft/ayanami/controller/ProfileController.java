// us.onnasoft.ayanami.controller.ProfileController
package us.onnasoft.ayanami.controller;

import us.onnasoft.ayanami.dto.ProfileUpdateRequest;
import us.onnasoft.ayanami.models.User;
import us.onnasoft.ayanami.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/profile")
public class ProfileController {
  private final ProfileService profileService;

  @Autowired
  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  /**
   * Obtiene el perfil del usuario autenticado.
   * 
   * @return El perfil del usuario si existe, o un 404 si no.
   */
  @GetMapping("")
  public ResponseEntity<Object> getUserProfile() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    User user = profileService.getUserProfileByUsername(username);
    if (user != null) {
      return ResponseEntity.ok(user);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Actualiza el perfil de un usuario.
   * 
   * @param userId      El ID del usuario.
   * @param updatedUser Los datos actualizados del usuario.
   * @return El perfil actualizado.
   */
  @PutMapping("")
  public ResponseEntity<User> updateUserProfile(@Valid @RequestBody ProfileUpdateRequest payload) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    User user = profileService.getUserProfileByUsername(username);
    if (user == null) {
      return ResponseEntity.notFound().build();
    }

    user.setName(payload.getName());
    user.setPhone(payload.getPhone());
    user.setBio(payload.getBio());
    user.setLocation(payload.getLocation());
    user.setWebsite(payload.getWebsite());
    user.setBirthDate(payload.getBirthDate());
    user.setGender(payload.getGender());

    return ResponseEntity.ok(profileService.updateUserProfile(user.getId(), user));
  }
}
