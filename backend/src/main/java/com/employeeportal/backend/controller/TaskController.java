package com.employeeportal.backend.controller;

import com.employeeportal.backend.entity.Task;
import com.employeeportal.backend.entity.User;
import com.employeeportal.backend.repository.TaskRepository;
import com.employeeportal.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/mine")
    public ResponseEntity<List<Task>> myTasks(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskRepository.findByAssignedToIdOrderByDueDateAsc(currentUser.getId()));
    }

    public record ProgressUpdateRequest(Task.Status status, Integer progressPercent) {}

    @PatchMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long id,
            @RequestBody ProgressUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {

        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        }
        Task task = taskOpt.get();
        if (!task.getAssignedTo().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only update your own tasks"));
        }
        if (request.status() != null) task.setStatus(request.status());
        if (request.progressPercent() != null) task.setProgressPercent(request.progressPercent());
        taskRepository.save(task);
        return ResponseEntity.ok(task);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Task> allTasks() {
        return taskRepository.findAll();
    }

    public record CreateTaskRequest(String title, String description, Long assignedToId,
                                     LocalDate dueDate, Task.Priority priority) {}

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest request, @AuthenticationPrincipal User currentUser) {
        Optional<User> assigneeOpt = userRepository.findById(request.assignedToId());
        if (assigneeOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Assigned employee not found"));
        }
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setAssignedTo(assigneeOpt.get());
        task.setAssignedBy(currentUser);
        task.setDueDate(request.dueDate());
        task.setPriority(request.priority());
        taskRepository.save(task);
        return ResponseEntity.ok(task);
    }
}
