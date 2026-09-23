package com.employeeportal.backend.controller;

import com.employeeportal.backend.entity.Attendance;
import com.employeeportal.backend.entity.User;
import com.employeeportal.backend.repository.AttendanceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;

    public AttendanceController(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@AuthenticationPrincipal User currentUser) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByUserIdAndWorkDate(currentUser.getId(), today);
        if (existing.isPresent() && existing.get().getClockOutTime() != null) {
            return ResponseEntity.status(409).body(Map.of("error", "You've already completed attendance for today"));
        }
        if (existing.isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "You're already clocked in today"));
        }
        Attendance attendance = new Attendance();
        attendance.setUser(currentUser);
        attendance.setWorkDate(today);
        attendance.setClockInTime(LocalDateTime.now());
        attendanceRepository.save(attendance);
        return ResponseEntity.ok(attendance);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@AuthenticationPrincipal User currentUser) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByUserIdAndWorkDate(currentUser.getId(), today);
        if (existing.isEmpty()) {
            return ResponseEntity.status(409).body(Map.of("error", "You haven't clocked in today yet"));
        }
        if (existing.get().getClockOutTime() != null) {
            return ResponseEntity.status(409).body(Map.of("error", "You've already clocked out today"));
        }
        Attendance attendance = existing.get();
        attendance.setClockOutTime(LocalDateTime.now());
        attendanceRepository.save(attendance);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/today")
    public ResponseEntity<?> today(@AuthenticationPrincipal User currentUser) {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(
            attendanceRepository.findByUserIdAndWorkDate(currentUser.getId(), today).orElse(null)
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<Attendance>> history(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(attendanceRepository.findByUserIdOrderByWorkDateDesc(currentUser.getId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-today")
    public ResponseEntity<List<Attendance>> allToday() {
        return ResponseEntity.ok(attendanceRepository.findByWorkDate(LocalDate.now()));
    }
}
