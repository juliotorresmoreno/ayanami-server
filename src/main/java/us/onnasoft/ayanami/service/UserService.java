package us.onnasoft.ayanami.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.ConstraintViolationException;
import us.onnasoft.ayanami.models.User;
import us.onnasoft.ayanami.models.User.Role;
import us.onnasoft.ayanami.repository.UserRepository;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MESSAGE));
    }

    @Transactional
    public User createUser(User payload) {
        if (userRepository.findByEmail(payload.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        user.setName(payload.getName());
        user.setUsername(payload.getUsername());
        user.setEmail(payload.getEmail());
        user.setPassword(payload.getPassword());
        user.setRole(payload.getRole() != null ? payload.getRole() : Role.USER);
        user.setActive(payload.getActive() != null ? payload.getActive() : Boolean.TRUE);

        try {
            return userRepository.save(payload);
        } catch (ConstraintViolationException e) {
            final String message = e.getConstraintViolations().stream()
                    .map(cv -> cv.getMessage())
                    .reduce("", (acc, cv) -> acc + cv + "\n");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user");
        }
    }

    @Transactional
    public User updateUser(Long id, User payload) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MESSAGE));

        try {
            user.setName(payload.getName());
            user.setEmail(payload.getEmail());

            if (payload.getPassword() != null && !payload.getPassword().isBlank()) {
                user.setPassword(payload.getPassword());
            }

            user.setRole(payload.getRole() != null ? payload.getRole() : user.getRole());
            user.setActive(payload.getActive() != null ? payload.getActive() : user.getActive());

            return userRepository.save(user);
        } catch (ConstraintViolationException e) {
            final String message = e.getConstraintViolations().stream()
                    .map(cv -> cv.getMessage())
                    .reduce("", (acc, cv) -> acc + cv + "\n");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user");
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MESSAGE);
        }
        try {
            userRepository.deleteById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user");
        }
    }

    @Transactional
    public User changePassword(Long id, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MESSAGE));

        try {
            user.setPassword(password);
            return userRepository.save(user);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to change password");
        }
    }
}
