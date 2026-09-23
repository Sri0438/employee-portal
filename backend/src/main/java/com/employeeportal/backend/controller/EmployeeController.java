package com.employeeportal.backend.controller;

import com.employeeportal.backend.entity.User;
import com.employeeportal.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<User> listEmployees() {
        return userRepository.findByRole(User.Role.EMPLOYEE);
    }

    public record CreateEmployeeRequest(String fullName, String email, String password) {}

    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody CreateEmployeeRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "An account with that email already exists"));
        }
        User employee = new User();
        employee.setFullName(request.fullName());
        employee.setEmail(request.email());
        employee.setPasswordHash(passwordEncoder.encode(request.password()));
        employee.setRole(User.Role.EMPLOYEE);
        employee.setActive(true);
        userRepository.save(employee);
        return ResponseEntity.ok(employee);
    }

    public record StatusUpdateRequest(boolean active) {}

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Employee not found"));
        }
        User user = userOpt.get();
        user.setActive(request.active());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}
