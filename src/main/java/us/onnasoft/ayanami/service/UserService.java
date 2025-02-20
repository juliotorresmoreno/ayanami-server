package us.onnasoft.ayanami.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import us.onnasoft.ayanami.dto.RegisterRequest;
import us.onnasoft.ayanami.models.User;
import us.onnasoft.ayanami.models.User.Role;
import us.onnasoft.ayanami.repository.UserRepository;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public User createUser(RegisterRequest userDTO) {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        user.setName(userDTO.getName());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : Role.USER);
        user.setActive(userDTO.getActive() != null ? userDTO.getActive() : true);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, RegisterRequest userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            user.setPassword(userDTO.getPassword());
        }

        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : user.getRole());
        user.setActive(userDTO.getActive() != null ? userDTO.getActive() : user.getActive());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }
}
