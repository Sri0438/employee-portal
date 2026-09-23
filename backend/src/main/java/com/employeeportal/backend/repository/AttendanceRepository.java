package com.employeeportal.backend.repository;

import com.employeeportal.backend.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserIdAndWorkDate(Long userId, LocalDate workDate);
    List<Attendance> findByUserIdOrderByWorkDateDesc(Long userId);
    List<Attendance> findByWorkDate(LocalDate workDate);
}
