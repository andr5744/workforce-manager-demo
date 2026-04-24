package com.example.workforce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "project_assignments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "project_id"}))
public class ProjectAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @NotBlank
    @Column(nullable = false)
    private String role;

    @NotNull
    @Positive
    @Column(name = "hours_allocated", nullable = false)
    private Integer hoursAllocated;

    @Column(name = "assigned_date")
    @Temporal(TemporalType.DATE)
    private Date assignedDate;

    public ProjectAssignment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getHoursAllocated() { return hoursAllocated; }
    public void setHoursAllocated(Integer hoursAllocated) { this.hoursAllocated = hoursAllocated; }

    public Date getAssignedDate() { return assignedDate; }
    public void setAssignedDate(Date assignedDate) { this.assignedDate = assignedDate; }
}
