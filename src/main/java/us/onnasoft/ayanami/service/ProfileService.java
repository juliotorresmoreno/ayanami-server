// us.onnasoft.ayanami.service.ProfileService
package us.onnasoft.ayanami.service;

import us.onnasoft.ayanami.models.User;
import us.onnasoft.ayanami.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtiene el perfil de un usuario por su ID.
     * 
     * @param userId El ID del usuario.
     * @return El usuario si existe, o un Optional vacío si no.
     */
    public Optional<User> getUserProfile(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Obtiene el perfil de un usuario por su nombre de usuario.
     * 
     * @param username El nombre de usuario (email o username).
     * @return El usuario si existe, o null si no.
     */
    public User getUserProfileByUsername(String username) {
        return userRepository.findByEmailOrUsername(username, username);
    }

    /**
     * Actualiza el perfil de un usuario.
     * 
     * @param userId      El ID del usuario.
     * @param updatedUser Los datos actualizados del usuario.
     * @return El usuario actualizado.
     * @throws RuntimeException Si el usuario no existe.
     */
    @Transactional
    public User updateUserProfile(Long userId, User updatedUser) {
        return userRepository.findById(userId).map(user -> {
            user.setName(updatedUser.getName());
            user.setEmail(updatedUser.getEmail());
            user.setBio(updatedUser.getBio());
            user.setLocation(updatedUser.getLocation());
            user.setWebsite(updatedUser.getWebsite());
            user.setBirthDate(updatedUser.getBirthDate());
            user.setGender(updatedUser.getGender());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    /**
     * Elimina el perfil de un usuario.
     * 
     * @param userId El ID del usuario.
     * @throws RuntimeException Si el usuario no existe.
     */
    @Transactional
    public void deleteUserProfile(Long userId) {
        userRepository.findById(userId).ifPresentOrElse(
                userRepository::delete,
                () -> {
                    throw new RuntimeException("User not found with id: " + userId);
                });
    }
}