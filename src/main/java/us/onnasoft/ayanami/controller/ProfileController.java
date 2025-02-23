// us.onnasoft.ayanami.controller.ProfileController
package us.onnasoft.ayanami.controller;

import us.onnasoft.ayanami.models.User;
import us.onnasoft.ayanami.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/profile")
public class ProfileController {

  @Autowired
  private ProfileService profileService;

  /**
   * Obtiene el perfil del usuario autenticado.
   * 
   * @return El perfil del usuario si existe, o un 404 si no.
   */
  @GetMapping("")
  public ResponseEntity<Object> getUserProfile() {
    System.out.println("GET /profile/");

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName(); // Obtiene el nombre de usuario (email o username)

    // Busca el usuario en la base de datos
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
  @PutMapping("/{userId}")
  public ResponseEntity<User> updateUserProfile(@PathVariable Long userId, @RequestBody User updatedUser) {
    try {
      User user = profileService.updateUserProfile(userId, updatedUser);
      return ResponseEntity.ok(user);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }
}