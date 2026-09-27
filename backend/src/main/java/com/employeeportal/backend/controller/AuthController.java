package com.employeeportal.backend.controller;

import com.employeeportal.backend.entity.User;
import com.employeeportal.backend.repository.UserRepository;
import com.employeeportal.backend.security.JwtService;
import com.employeeportal.backend.security.LoginAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           JwtService jwtService, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }

    public record LoginRequest(String email, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String email = request.email();

        if (loginAttemptService.isLocked(email)) {
            long minutes = loginAttemptService.minutesRemaining(email);
            return ResponseEntity.status(429).body(Map.of(
                "error", "Too many failed attempts. Try again in " + minutes + " minute(s)."
            ));
        }

        var userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty() || !passwordEncoder.matches(request.password(), userOpt.get().getPasswordHash())) {
            loginAttemptService.recordFailure(email);
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            return ResponseEntity.status(403).body(Map.of("error", "Account is deactivated"));
        }

        loginAttemptService.recordSuccess(email);
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(Map.of(
            "token", token,
            "fullName", user.getFullName(),
            "role", user.getRole().name()
        ));
    }
}
