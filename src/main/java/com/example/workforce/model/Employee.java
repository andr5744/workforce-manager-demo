package com.example.workforce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @NotNull
    @Column(name = "hire_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date hireDate;

    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal salary;

    @Column(name = "job_title")
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @JsonIgnore
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private List<Employee> directReports = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimeEntry> timeEntries = new ArrayList<>();

    public enum EmployeeStatus {
        ACTIVE, INACTIVE, ON_LEAVE, TERMINATED
    }

    public Employee() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Date getHireDate() { return hireDate; }
    public void setHireDate(Date hireDate) { this.hireDate = hireDate; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public EmployeeStatus getStatus() { return status; }
    public void setStatus(EmployeeStatus status) { this.status = status; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Employee getManager() { return manager; }
    public void setManager(Employee manager) { this.manager = manager; }

    public List<Employee> getDirectReports() { return directReports; }
    public void setDirectReports(List<Employee> directReports) { this.directReports = directReports; }

    public List<TimeEntry> getTimeEntries() { return timeEntries; }
    public void setTimeEntries(List<TimeEntry> timeEntries) { this.timeEntries = timeEntries; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
