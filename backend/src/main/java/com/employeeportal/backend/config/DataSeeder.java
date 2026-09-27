package com.employeeportal.backend.config;

import com.employeeportal.backend.entity.User;
import com.employeeportal.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.name:}")
    private String adminName;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            System.out.println(">>> ADMIN_EMAIL/ADMIN_PASSWORD not set — skipping admin seeding.");
            return;
        }
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setFullName(adminName.isBlank() ? "Admin" : adminName);
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(User.Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println(">>> Seeded admin account: " + adminEmail);
        } else {
            System.out.println(">>> Admin account already exists, skipping seeding: " + adminEmail);
        }
    }
}
