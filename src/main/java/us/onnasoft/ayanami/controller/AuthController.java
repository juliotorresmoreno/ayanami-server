package us.onnasoft.ayanami.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${url_base}")
    private String baseUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest payload) {
        final User user = userService.createUser(payload);
        final String token = jwtUtil.generateToken(user.getEmail());
        final String verificationLink = baseUrl + "/auth/verify-email?token=" + token;

        Map<String, Object> model = new HashMap<>();
        model.put("username", user.getUsername());
        model.put("verificationLink", verificationLink);

        emailService.sendTemplateEmail(
                user.getEmail(),
                "Email Verification",
                "email-verification",
                model);

        final RegisterResponse response = new RegisterResponse();
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setActive(user.getActive());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty() || !userOptional.get().isPasswordValid(request.getPassword())) {
            return ResponseEntity.status(401).body(
                    new LoginResponse("Invalid email or password", null));
        }

        String token = jwtUtil.generateToken(userOptional.get().getEmail());
        return ResponseEntity.ok(new LoginResponse("Login successful", token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody PasswordResetRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.ok("If the email exists, a password reset link has been sent.");
        }

        String resetToken = jwtUtil.generateToken(userOptional.get().getEmail());
        String resetLink = baseUrl + "/auth/reset-password?token=" + resetToken;

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
