package us.onnasoft.ayanami.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import us.onnasoft.ayanami.dto.*;
import us.onnasoft.ayanami.exceptions.ApiErrorResponse;
import us.onnasoft.ayanami.models.User;
import us.onnasoft.ayanami.models.User.Role;
import us.onnasoft.ayanami.repository.UserRepository;
import us.onnasoft.ayanami.security.JwtUtil;
import us.onnasoft.ayanami.service.EmailService;
import us.onnasoft.ayanami.service.LoginAttemptService;
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
    private static final String USER_NOT_AUTHENTICATED = "User not authenticated";

    @Value("${url_base}")
    private String baseUrl;

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final LoginAttemptService loginAttemptService;

    @Autowired
    public AuthController(UserService userService, UserRepository userRepository, JwtUtil jwtUtil,
            EmailService emailService, LoginAttemptService loginAttemptService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.loginAttemptService = loginAttemptService;
    }

    private final Logger logger = LogManager.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest payload,
            HttpServletRequest request) {
        logger.info("Starting registration for user: {}", payload.getEmail());

        User user = new User();
        user.setName(payload.getName());
        user.setUsername(payload.getUsername());
        user.setEmail(payload.getEmail());
        user.setPassword(payload.getPassword());
        user.setRole(payload.getRole() != null ? payload.getRole() : Role.USER);
        user.setActive(payload.getActive() != null ? payload.getActive() : Boolean.TRUE);

        user = userService.createUser(user);
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
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest payload, HttpServletRequest request) {
        logger.info("Login attempt for user: {}", payload.getEmail());

        final String ipAddress = request.getRemoteAddr();
        final Optional<User> userOptional = userRepository.findByEmail(payload.getEmail());

        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(401, "Invalid email or password", null);
        }

        final User user = userOptional.get();
        final Long userId = user.getId();
        if (!user.isPasswordValid(payload.getPassword())) {
            logger.warn("Invalid login attempt for user: {}", payload.getEmail());
            loginAttemptService.recordLoginAttempt(userId, ipAddress, false);
        }

        loginAttemptService.recordLoginAttempt(userId, ipAddress, true);

        final String token = jwtUtil.generateToken(user.getEmail());
        logger.info("User logged in successfully: {}", payload.getEmail());
        return ResponseEntity.ok(new LoginResponse("Login successful", token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest payload,
            HttpServletRequest request) {
        logger.info("Password reset requested for user: {}", payload.getEmail());

        final Optional<User> userOptional = userRepository.findByEmail(payload.getEmail());
        if (userOptional.isEmpty()) {
            logger.warn("Password reset request for non-existent user: {}", payload.getEmail());
            final String message = "If the email exists, a password reset link has been sent.";
            throw new ResponseStatusException(400, message, null);
        }

        final String resetToken = jwtUtil.generateToken(userOptional.get().getEmail());
        final String resetLink = baseUrl + "/auth/reset-password?token=" + resetToken;

        logger.info("Sending password reset link to: {}", payload.getEmail());
        emailService.sendEmail(
                payload.getEmail(),
                "Password Reset",
                "Click the link to reset your password: " + resetLink);

        final String message = "If the email exists, a password reset link has been sent.";
        return ResponseEntity.ok(new ForgotPasswordResponse(message));
    }

    @GetMapping("/reset-password")
    public ResponseEntity<String> showResetPasswordPage(HttpServletRequest request) throws IOException {
        logger.info("Loading reset password page");

        final ClassPathResource htmlFile = new ClassPathResource("static/reset-password.html");
        final String content = new String(htmlFile.getInputStream().readAllBytes());
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(content);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody PasswordResetConfirm payload,
            HttpServletRequest request) {
        logger.info("Password reset attempt for token: {}", payload.getToken());

        if (!jwtUtil.isTokenValid(payload.getToken())) {
            logger.warn("Invalid or expired token: {}", payload.getToken());
            throw new ApiErrorResponse(
                    LocalDateTime.now(),
                    400,
                    "Invalid or expired token",
                    request.getRequestURI());
        }

        final String email = jwtUtil.extractEmail(payload.getToken());
        final Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            logger.warn("User not found for password reset: {}", email);
            throw new ResponseStatusException(404, "User not found", null);
        }

        final User user = userOptional.get();
        user.setPassword(payload.getNewPassword());
        userRepository.save(user);

        logger.info("Password reset successful for user: {}", email);
        return ResponseEntity.ok(new ResetPasswordResponse("Password reset successful."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest payload, HttpServletRequest request) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal().toString().equals("anonymousUser")) {
            logger.warn(USER_NOT_AUTHENTICATED);
            throw new ResponseStatusException(401, USER_NOT_AUTHENTICATED, null);
        }

        final Optional<User> userOptional = userRepository.findByEmail(authentication.getName());
        final String currentPassword = payload.getCurrentPassword();
        final String newPassword = payload.getNewPassword();
        if (userOptional.isEmpty() || !userOptional.get().isPasswordValid(currentPassword)) {
            logger.warn("Invalid password change attempt for user: {}", authentication.getName());
            throw new ResponseStatusException(401, "Invalid password", null);
        }

        final User user = userOptional.get();
        userService.changePassword(user.getId(), newPassword);

        return ResponseEntity.ok(new ChangePasswordResponse("Password changed successfully."));
    }

    @GetMapping("/login-attempts")
    public ResponseEntity<LoginAttemptsResponse> getLoginAttempts(HttpServletRequest request) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal().toString().equals("anonymousUser")) {
            logger.warn(USER_NOT_AUTHENTICATED);
            throw new ResponseStatusException(401, USER_NOT_AUTHENTICATED, null);
        }

        final Optional<User> userOptional = userRepository.findByEmail(authentication.getName());
        if (userOptional.isEmpty()) {
            logger.warn("User not found: {}", authentication.getName());
            throw new ResponseStatusException(404, "User not found", null);
        }

        final User user = userOptional.get();
        final Long userId = user.getId();
        final LoginAttemptsResponse response = new LoginAttemptsResponse();
        
        System.out.println(loginAttemptService.getLoginAttemptsByUserId(userId));

        response.setLoginAttempts(loginAttemptService.getLoginAttemptsByUserId(userId));

        return ResponseEntity.ok(response);
    }
}
