package us.onnasoft.ayanami.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.io.IOException;
import us.onnasoft.ayanami.dto.*;
import org.springframework.util.StreamUtils;
import us.onnasoft.ayanami.models.User;
import us.onnasoft.ayanami.repository.UserRepository;
import us.onnasoft.ayanami.security.JwtUtil;
import us.onnasoft.ayanami.service.EmailService;
import us.onnasoft.ayanami.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserDTO userDTO) {
        User user = userService.createUser(userDTO);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty() || !userOptional.get().isPasswordValid(request.getPassword())) {
            return ResponseEntity.status(401).body(
                    new AuthResponse("Invalid email or password", null));
        }

        String token = jwtUtil.generateToken(userOptional.get().getEmail());
        return ResponseEntity.ok(new AuthResponse("Login successful", token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody PasswordResetRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.ok("If the email exists, a password reset link has been sent.");
        }

        String resetToken = jwtUtil.generateToken(userOptional.get().getEmail());
        String resetLink = "http://localhost:8080/auth/reset-password?token=" + resetToken;

        System.out.println("Reset link: " + resetLink + " for email: " + request.getEmail());
        emailService.sendEmail(
                request.getEmail(),
                "Password Reset",
                "Click the link to reset your password: " + resetLink);

        return ResponseEntity.ok("If the email exists, a password reset link has been sent.");
    }

    @GetMapping("/reset-password")
    public ResponseEntity<String> showResetPasswordPage() throws Exception {
        ClassPathResource htmlFile = new ClassPathResource("static/reset-password.html");
        String content;
        try {
            content = StreamUtils.copyToString(htmlFile.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error loading reset password page.");
        }
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(content);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetConfirm request) {
        if (!jwtUtil.isTokenValid(request.getToken())) {
            return ResponseEntity.status(400).body("Invalid or expired token.");
        }

        String email = jwtUtil.extractEmail(request.getToken());
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("User not found.");
        }

        User user = userOptional.get();
        user.setPassword(request.getNewPassword());
        userRepository.save(user);

        return ResponseEntity.ok("Password reset successful.");
    }
}
