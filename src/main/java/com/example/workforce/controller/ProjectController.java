package com.example.workforce.controller;

import com.example.workforce.model.Project;
import com.example.workforce.model.Project.ProjectStatus;
import com.example.workforce.model.ProjectAssignment;
import com.example.workforce.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody Project project) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id,
                                                  @Valid @RequestBody Project project) {
        return ResponseEntity.ok(projectService.updateProject(id, project));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Project> updateStatus(@PathVariable Long id,
                                                 @RequestParam ProjectStatus status) {
        return ResponseEntity.ok(projectService.updateProjectStatus(id, status));
    }

    @PostMapping("/{id}/expenses")
    public ResponseEntity<Project> recordExpense(@PathVariable Long id,
                                                  @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(projectService.recordExpense(id, amount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/assignments")
    public ResponseEntity<List<ProjectAssignment>> getAssignments(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getAssignmentsForProject(id));
    }

    @PostMapping("/{id}/assignments")
    public ResponseEntity<ProjectAssignment> assignEmployee(
            @PathVariable Long id,
            @RequestParam Long employeeId,
            @RequestParam String role,
            @RequestParam Integer hoursAllocated) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.assignEmployee(id, employeeId, role, hoursAllocated));
    }

    @DeleteMapping("/{id}/assignments/{employeeId}")
    public ResponseEntity<Void> removeAssignment(@PathVariable Long id,
                                                  @PathVariable Long employeeId) {
        projectService.removeAssignment(id, employeeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Project>> getByStatus(@PathVariable ProjectStatus status) {
        return ResponseEntity.ok(projectService.getProjectsByStatus(status));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Project>> getOverdueProjects() {
        return ResponseEntity.ok(projectService.getOverdueProjects());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Project>> getProjectsForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(projectService.getProjectsForEmployee(employeeId));
    }
}
