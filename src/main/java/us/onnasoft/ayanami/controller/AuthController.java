package us.onnasoft.ayanami.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import us.onnasoft.ayanami.dto.*;
import us.onnasoft.ayanami.exceptions.ApiErrorResponse;
import us.onnasoft.ayanami.models.User;
import us.onnasoft.ayanami.repository.UserRepository;
import us.onnasoft.ayanami.security.JwtUtil;
import us.onnasoft.ayanami.service.EmailService;
import us.onnasoft.ayanami.service.UserService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private final Logger logger = LogManager.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest payload, HttpServletRequest request) {
        logger.info("Starting registration for user: {}", payload.getEmail());

        try {
            final User user = userService.createUser(payload);
            final String token = jwtUtil.generateToken(user.getEmail());
            final String verificationLink = baseUrl + "/auth/verify-email?token=" + token;

            final Map<String, Object> model = new HashMap<>();
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

            logger.info("User registered successfully: {}", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            final ApiErrorResponse errorResponse = new ApiErrorResponse(request.getRequestURI(), e);
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest payload, HttpServletRequest request) {
        logger.info("Login attempt for user: {}", payload.getEmail());

        try {
            final Optional<User> userOptional = userRepository.findByEmail(payload.getEmail());

            if (userOptional.isEmpty() || !userOptional.get().isPasswordValid(payload.getPassword())) {
                logger.warn("Invalid login attempt for user: {}", payload.getEmail());
                final ApiErrorResponse errorResponse = new ApiErrorResponse(
                        LocalDateTime.now(),
                        401,
                        "Invalid email or password",
                        request.getRequestURI());
                return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
            }

            final String token = jwtUtil.generateToken(userOptional.get().getEmail());
            logger.info("User logged in successfully: {}", payload.getEmail());
            return ResponseEntity.ok(new LoginResponse("Login successful", token));
        } catch (Exception e) {
            logger.error("Error during login for user: {}", payload.getEmail(), e);
            final ApiErrorResponse errorResponse = new ApiErrorResponse(request.getRequestURI(), e);
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest payload,
            HttpServletRequest request) {
        logger.info("Password reset requested for user: {}", payload.getEmail());

        try {
            final Optional<User> userOptional = userRepository.findByEmail(payload.getEmail());
            if (userOptional.isEmpty()) {
                logger.warn("Password reset request for non-existent user: {}", payload.getEmail());
                return ResponseEntity.ok("If the email exists, a password reset link has been sent.");
            }

            final String resetToken = jwtUtil.generateToken(userOptional.get().getEmail());
            final String resetLink = baseUrl + "/auth/reset-password?token=" + resetToken;

            logger.info("Sending password reset link to: {}", payload.getEmail());
            emailService.sendEmail(
                payload.getEmail(),
                    "Password Reset",
                    "Click the link to reset your password: " + resetLink);

            return ResponseEntity.ok(
                    new ForgotPasswordResponse("If the email exists, a password reset link has been sent."));
        } catch (Exception e) {
            logger.error("Error during password reset for user: {}", payload.getEmail(), e);
            final ApiErrorResponse errorResponse = new ApiErrorResponse(request.getRequestURI(), e);
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        }
    }

    @GetMapping("/reset-password")
    public ResponseEntity<?> showResetPasswordPage(HttpServletRequest request) throws IOException {
        logger.info("Loading reset password page");

        try {
            final ClassPathResource htmlFile = new ClassPathResource("static/reset-password.html");
            final String content = new String(htmlFile.getInputStream().readAllBytes());
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(content);
        } catch (IOException e) {
            logger.error("Error loading reset password page", e);
            final ApiErrorResponse errorResponse = new ApiErrorResponse(request.getRequestURI(), e);
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetConfirm payload, HttpServletRequest request) {
        logger.info("Password reset attempt for token: {}", payload.getToken());

        try {
            if (!jwtUtil.isTokenValid(payload.getToken())) {
                logger.warn("Invalid or expired token: {}", payload.getToken());
                final ApiErrorResponse errorResponse = new ApiErrorResponse(
                        LocalDateTime.now(),
                        400,
                        "Invalid or expired token",
                        request.getRequestURI());
                return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse.toString());
            }

            final String email = jwtUtil.extractEmail(payload.getToken());
            final Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                logger.warn("User not found for password reset: {}", email);
                final ApiErrorResponse errorResponse = new ApiErrorResponse(
                        LocalDateTime.now(),
                        404,
                        "User not found",
                        request.getRequestURI());
                return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse.toString());
            }

            final User user = userOptional.get();
            user.setPassword(payload.getNewPassword());
            userRepository.save(user);

            logger.info("Password reset successful for user: {}", email);
            return ResponseEntity.ok("Password reset successful.");
        } catch (Exception e) {
            logger.error("Error during password reset for token: {}", payload.getToken(), e);
            final ApiErrorResponse errorResponse = new ApiErrorResponse(request.getRequestURI(), e);
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        }
    }
}