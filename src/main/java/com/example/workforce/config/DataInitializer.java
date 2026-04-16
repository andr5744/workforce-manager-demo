package com.example.workforce.config;

import com.example.workforce.model.Department;
import com.example.workforce.model.Employee;
import com.example.workforce.model.Project;
import com.example.workforce.model.ProjectAssignment;
import com.example.workforce.model.TimeEntry;
import com.example.workforce.repository.DepartmentRepository;
import com.example.workforce.repository.EmployeeRepository;
import com.example.workforce.repository.ProjectAssignmentRepository;
import com.example.workforce.repository.ProjectRepository;
import com.example.workforce.repository.TimeEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectAssignmentRepository assignmentRepository;
    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @Override
    public void run(String... args) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // --- Departments ---
        Department engineering = departmentRepository.save(
                new Department("Engineering", "Building A", new BigDecimal("500000"), 50));
        Department marketing = departmentRepository.save(
                new Department("Marketing", "Building B", new BigDecimal("200000"), 20));
        Department hr = departmentRepository.save(
                new Department("Human Resources", "Building C", new BigDecimal("150000"), 15));
        Department finance = departmentRepository.save(
                new Department("Finance", "Building A", new BigDecimal("180000"), 12));

        // --- Employees ---
        Employee alice = new Employee();
        alice.setFirstName("Alice"); alice.setLastName("Johnson");
        alice.setEmail("alice.johnson@company.com");
        alice.setJobTitle("VP of Engineering");
        alice.setHireDate(sdf.parse("2016-04-01"));
        alice.setSalary(new BigDecimal("145000"));
        alice.setDepartment(engineering);
        alice.setStatus(Employee.EmployeeStatus.ACTIVE);
        alice = employeeRepository.save(alice);

        Employee bob = new Employee();
        bob.setFirstName("Bob"); bob.setLastName("Smith");
        bob.setEmail("bob.smith@company.com");
        bob.setJobTitle("Senior Software Engineer");
        bob.setHireDate(sdf.parse("2018-07-15"));
        bob.setSalary(new BigDecimal("98000"));
        bob.setDepartment(engineering);
        bob.setManager(alice);
        bob.setStatus(Employee.EmployeeStatus.ACTIVE);
        bob = employeeRepository.save(bob);

        Employee carol = new Employee();
        carol.setFirstName("Carol"); carol.setLastName("Williams");
        carol.setEmail("carol.williams@company.com");
        carol.setJobTitle("Software Engineer");
        carol.setHireDate(sdf.parse("2021-03-22"));
        carol.setSalary(new BigDecimal("78000"));
        carol.setDepartment(engineering);
        carol.setManager(alice);
        carol.setStatus(Employee.EmployeeStatus.ACTIVE);
        carol = employeeRepository.save(carol);

        Employee dave = new Employee();
        dave.setFirstName("Dave"); dave.setLastName("Brown");
        dave.setEmail("dave.brown@company.com");
        dave.setJobTitle("Marketing Director");
        dave.setHireDate(sdf.parse("2017-11-10"));
        dave.setSalary(new BigDecimal("110000"));
        dave.setDepartment(marketing);
        dave.setStatus(Employee.EmployeeStatus.ACTIVE);
        dave = employeeRepository.save(dave);

        Employee eve = new Employee();
        eve.setFirstName("Eve"); eve.setLastName("Davis");
        eve.setEmail("eve.davis@company.com");
        eve.setJobTitle("Marketing Specialist");
        eve.setHireDate(sdf.parse("2020-01-06"));
        eve.setSalary(new BigDecimal("58000"));
        eve.setDepartment(marketing);
        eve.setManager(dave);
        eve.setStatus(Employee.EmployeeStatus.ACTIVE);
        eve = employeeRepository.save(eve);

        Employee frank = new Employee();
        frank.setFirstName("Frank"); frank.setLastName("Wilson");
        frank.setEmail("frank.wilson@company.com");
        frank.setJobTitle("HR Manager");
        frank.setHireDate(sdf.parse("2015-08-20"));
        frank.setSalary(new BigDecimal("88000"));
        frank.setDepartment(hr);
        frank.setStatus(Employee.EmployeeStatus.ACTIVE);
        frank = employeeRepository.save(frank);

        Employee grace = new Employee();
        grace.setFirstName("Grace"); grace.setLastName("Lee");
        grace.setEmail("grace.lee@company.com");
        grace.setJobTitle("Junior Software Engineer");
        grace.setHireDate(sdf.parse("2023-06-05"));
        grace.setSalary(new BigDecimal("62000"));
        grace.setDepartment(engineering);
        grace.setManager(bob);
        grace.setStatus(Employee.EmployeeStatus.ACTIVE);
        grace = employeeRepository.save(grace);

        // --- Projects ---
        Project projectAlpha = new Project();
        projectAlpha.setName("Project Alpha");
        projectAlpha.setDescription("Customer portal modernization and redesign");
        projectAlpha.setStartDate(sdf.parse("2024-01-15"));
        projectAlpha.setEndDate(sdf.parse("2024-12-31"));
        projectAlpha.setStatus(Project.ProjectStatus.IN_PROGRESS);
        projectAlpha.setBudget(new BigDecimal("300000"));
        projectAlpha.setBudgetSpent(new BigDecimal("95000"));
        projectAlpha.setClientName("Acme Corp");
        projectAlpha = projectRepository.save(projectAlpha);

        Project projectBeta = new Project();
        projectBeta.setName("Project Beta");
        projectBeta.setDescription("Internal analytics and reporting dashboard");
        projectBeta.setStartDate(sdf.parse("2024-03-01"));
        projectBeta.setEndDate(sdf.parse("2024-09-30"));
        projectBeta.setStatus(Project.ProjectStatus.IN_PROGRESS);
        projectBeta.setBudget(new BigDecimal("120000"));
        projectBeta.setBudgetSpent(new BigDecimal("40000"));
        projectBeta.setClientName("Internal");
        projectBeta = projectRepository.save(projectBeta);

        Project projectGamma = new Project();
        projectGamma.setName("Project Gamma");
        projectGamma.setDescription("Supply chain integration API");
        projectGamma.setStartDate(sdf.parse("2023-06-01"));
        projectGamma.setEndDate(sdf.parse("2024-02-28"));
        projectGamma.setStatus(Project.ProjectStatus.COMPLETED);
        projectGamma.setBudget(new BigDecimal("200000"));
        projectGamma.setBudgetSpent(new BigDecimal("185000"));
        projectGamma.setClientName("Global Logistics Ltd");
        projectGamma = projectRepository.save(projectGamma);

        // --- Assignments ---
        ProjectAssignment a1 = new ProjectAssignment();
        a1.setEmployee(bob); a1.setProject(projectAlpha);
        a1.setRole("Lead Developer"); a1.setHoursAllocated(800);
        a1.setAssignedDate(sdf.parse("2024-01-15"));
        assignmentRepository.save(a1);

        ProjectAssignment a2 = new ProjectAssignment();
        a2.setEmployee(carol); a2.setProject(projectAlpha);
        a2.setRole("Frontend Developer"); a2.setHoursAllocated(600);
        a2.setAssignedDate(sdf.parse("2024-01-15"));
        assignmentRepository.save(a2);

        ProjectAssignment a3 = new ProjectAssignment();
        a3.setEmployee(grace); a3.setProject(projectAlpha);
        a3.setRole("Junior Developer"); a3.setHoursAllocated(400);
        a3.setAssignedDate(sdf.parse("2024-02-01"));
        assignmentRepository.save(a3);

        ProjectAssignment a4 = new ProjectAssignment();
        a4.setEmployee(bob); a4.setProject(projectBeta);
        a4.setRole("Backend Developer"); a4.setHoursAllocated(250);
        a4.setAssignedDate(sdf.parse("2024-03-01"));
        assignmentRepository.save(a4);

        ProjectAssignment a5 = new ProjectAssignment();
        a5.setEmployee(alice); a5.setProject(projectBeta);
        a5.setRole("Technical Lead"); a5.setHoursAllocated(100);
        a5.setAssignedDate(sdf.parse("2024-03-01"));
        assignmentRepository.save(a5);

        // --- Time Entries ---
        saveTimeEntry(sdf, bob, projectAlpha, "2024-06-10", 8, "REST API endpoint development",
                TimeEntry.BillableStatus.BILLABLE);
        saveTimeEntry(sdf, carol, projectAlpha, "2024-06-10", 7, "React component updates",
                TimeEntry.BillableStatus.BILLABLE);
        saveTimeEntry(sdf, grace, projectAlpha, "2024-06-10", 6, "Unit test implementation",
                TimeEntry.BillableStatus.BILLABLE);
        saveTimeEntry(sdf, bob, projectBeta, "2024-06-11", 4, "Database schema design",
                TimeEntry.BillableStatus.NON_BILLABLE);
        saveTimeEntry(sdf, alice, projectBeta, "2024-06-11", 2, "Architecture review meeting",
                TimeEntry.BillableStatus.INTERNAL);
        saveTimeEntry(sdf, carol, projectAlpha, "2024-06-12", 8, "UI/UX fixes from QA feedback",
                TimeEntry.BillableStatus.BILLABLE);
        saveTimeEntry(sdf, bob, projectAlpha, "2024-06-13", 7, "Security audit remediation",
                TimeEntry.BillableStatus.BILLABLE);
    }

    private void saveTimeEntry(SimpleDateFormat sdf, Employee employee, Project project,
                                String dateStr, int hours, String description,
                                TimeEntry.BillableStatus status) throws Exception {
        TimeEntry entry = new TimeEntry();
        entry.setEmployee(employee);
        entry.setProject(project);
        entry.setDate(sdf.parse(dateStr));
        entry.setHoursWorked(hours);
        entry.setDescription(description);
        entry.setBillableStatus(status);
        timeEntryRepository.save(entry);
    }
}
