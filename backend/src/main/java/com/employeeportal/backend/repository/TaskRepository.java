package com.employeeportal.backend.repository;

import com.employeeportal.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedToIdOrderByDueDateAsc(Long userId);
}
