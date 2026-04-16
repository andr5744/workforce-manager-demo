package com.example.workforce.service;

import com.example.workforce.exception.ResourceNotFoundException;
import com.example.workforce.model.Employee;
import com.example.workforce.model.Project;
import com.example.workforce.model.Project.ProjectStatus;
import com.example.workforce.model.ProjectAssignment;
import com.example.workforce.repository.EmployeeRepository;
import com.example.workforce.repository.ProjectAssignmentRepository;
import com.example.workforce.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectAssignmentRepository assignmentRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          EmployeeRepository employeeRepository,
                          ProjectAssignmentRepository assignmentRepository) {
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public Project updateProject(Long id, Project projectDetails) {
        Project project = getProjectById(id);
        project.setName(projectDetails.getName());
        project.setDescription(projectDetails.getDescription());
        project.setStartDate(projectDetails.getStartDate());
        project.setEndDate(projectDetails.getEndDate());
        project.setStatus(projectDetails.getStatus());
        project.setBudget(projectDetails.getBudget());
        project.setClientName(projectDetails.getClientName());
        return projectRepository.save(project);
    }

    public Project updateProjectStatus(Long id, ProjectStatus status) {
        Project project = getProjectById(id);
        project.setStatus(status);
        return projectRepository.save(project);
    }

    public Project recordExpense(Long id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be positive");
        }
        Project project = getProjectById(id);
        BigDecimal newSpent = project.getBudgetSpent().add(amount);
        if (newSpent.compareTo(project.getBudget()) > 0) {
            throw new IllegalArgumentException(
                    "Expense of " + amount + " exceeds remaining budget of " + project.getRemainingBudget());
        }
        project.setBudgetSpent(newSpent);
        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        if (project.getStatus() == ProjectStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot delete a project that is currently in progress");
        }
        projectRepository.delete(project);
    }

    public ProjectAssignment assignEmployee(Long projectId, Long employeeId,
                                             String role, Integer hoursAllocated) {
        if (assignmentRepository.existsByEmployeeIdAndProjectId(employeeId, projectId)) {
            throw new IllegalArgumentException("Employee is already assigned to this project");
        }
        Project project = getProjectById(projectId);
        if (project.getStatus() == ProjectStatus.COMPLETED ||
                project.getStatus() == ProjectStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot assign employees to a completed or cancelled project");
        }
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(project);
        assignment.setEmployee(employee);
        assignment.setRole(role);
        assignment.setHoursAllocated(hoursAllocated);
        assignment.setAssignedDate(new Date());
        return assignmentRepository.save(assignment);
    }

    public void removeAssignment(Long projectId, Long employeeId) {
        ProjectAssignment assignment = assignmentRepository
                .findByEmployeeIdAndProjectId(employeeId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProjectAssignment", "employeeId+projectId", employeeId + "+" + projectId));
        assignmentRepository.delete(assignment);
    }

    @Transactional(readOnly = true)
    public List<Project> getProjectsByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Project> getOverdueProjects() {
        return projectRepository.findOverdueProjects(new Date());
    }

    @Transactional(readOnly = true)
    public List<Project> getProjectsForEmployee(Long employeeId) {
        return projectRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<ProjectAssignment> getAssignmentsForProject(Long projectId) {
        return assignmentRepository.findByProjectId(projectId);
    }
}
