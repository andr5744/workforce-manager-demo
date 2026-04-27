package com.example.workforce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "time_entries")
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @NotNull
    @Min(1)
    @Max(24)
    @Column(name = "hours_worked", nullable = false)
    private Integer hoursWorked;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column
    private BillableStatus billableStatus = BillableStatus.BILLABLE;

    public enum BillableStatus {
        BILLABLE, NON_BILLABLE, INTERNAL
    }

    public TimeEntry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Integer getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(Integer hoursWorked) { this.hoursWorked = hoursWorked; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BillableStatus getBillableStatus() { return billableStatus; }
    public void setBillableStatus(BillableStatus billableStatus) { this.billableStatus = billableStatus; }
}
