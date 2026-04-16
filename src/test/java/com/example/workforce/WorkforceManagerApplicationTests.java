package com.example.workforce;

import com.example.workforce.model.Department;
import com.example.workforce.model.Employee;
import com.example.workforce.model.Project;
import com.example.workforce.repository.DepartmentRepository;
import com.example.workforce.repository.EmployeeRepository;
import com.example.workforce.repository.ProjectRepository;
import com.example.workforce.service.DepartmentService;
import com.example.workforce.service.EmployeeService;
import com.example.workforce.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class WorkforceManagerApplicationTests {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void contextLoads() {
        assertNotNull(departmentService);
        assertNotNull(employeeService);
        assertNotNull(projectService);
    }

    @Test
    void testCreateAndRetrieveDepartment() {
        Department dept = new Department("Test Dept", "Floor 1", new BigDecimal("100000"), 10);
        Department saved = departmentService.createDepartment(dept);

        assertNotNull(saved.getId());
        assertEquals("Test Dept", saved.getName());

        Department retrieved = departmentService.getDepartmentById(saved.getId());
        assertEquals("Floor 1", retrieved.getLocation());
    }

    @Test
    void testDuplicateDepartmentNameThrows() {
        Department dept1 = new Department("Unique Dept", "A", new BigDecimal("50000"), 5);
        departmentService.createDepartment(dept1);

        Department dept2 = new Department("Unique Dept", "B", new BigDecimal("60000"), 5);
        assertThrows(IllegalArgumentException.class, () -> departmentService.createDepartment(dept2));
    }

    @Test
    void testCreateEmployeeAndSearch() {
        Department dept = departmentRepository.save(
                new Department("Sales", "Floor 2", new BigDecimal("80000"), 8));

        Employee emp = new Employee();
        emp.setFirstName("John");
        emp.setLastName("Doe");
        emp.setEmail("john.doe.test@company.com");
        emp.setJobTitle("Sales Rep");
        emp.setHireDate(new Date());
        emp.setSalary(new BigDecimal("55000"));
        emp.setDepartment(dept);

        Employee saved = employeeService.createEmployee(emp);
        assertNotNull(saved.getId());

        List<Employee> results = employeeService.searchEmployees("John");
        assertTrue(results.stream().anyMatch(e -> e.getEmail().equals("john.doe.test@company.com")));
    }

    @Test
    void testDuplicateEmailThrows() {
        Department dept = departmentRepository.save(
                new Department("Ops", "Floor 3", new BigDecimal("70000"), 5));

        Employee emp1 = new Employee();
        emp1.setFirstName("A"); emp1.setLastName("B");
        emp1.setEmail("duplicate@company.com");
        emp1.setHireDate(new Date());
        emp1.setSalary(new BigDecimal("50000"));
        emp1.setDepartment(dept);
        employeeService.createEmployee(emp1);

        Employee emp2 = new Employee();
        emp2.setFirstName("C"); emp2.setLastName("D");
        emp2.setEmail("duplicate@company.com");
        emp2.setHireDate(new Date());
        emp2.setSalary(new BigDecimal("50000"));
        emp2.setDepartment(dept);

        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(emp2));
    }

    @Test
    void testCreateProjectAndUpdateStatus() {
        Project project = new Project();
        project.setName("Test Project");
        project.setDescription("A test project for unit tests");
        project.setStartDate(new Date());
        project.setBudget(new BigDecimal("50000"));
        project.setStatus(Project.ProjectStatus.PLANNING);

        Project saved = projectService.createProject(project);
        assertNotNull(saved.getId());
        assertEquals(Project.ProjectStatus.PLANNING, saved.getStatus());

        Project updated = projectService.updateProjectStatus(saved.getId(), Project.ProjectStatus.IN_PROGRESS);
        assertEquals(Project.ProjectStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void testRecordExpenseExceedsBudgetThrows() {
        Project project = new Project();
        project.setName("Budget Test Project");
        project.setStartDate(new Date());
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(Project.ProjectStatus.IN_PROGRESS);
        Project saved = projectService.createProject(project);

        assertThrows(IllegalArgumentException.class,
                () -> projectService.recordExpense(saved.getId(), new BigDecimal("2000")));
    }

    @Test
    void testEmployeeStatusUpdate() {
        Department dept = departmentRepository.save(
                new Department("Temp Dept", "Annex", new BigDecimal("30000"), 3));

        Employee emp = new Employee();
        emp.setFirstName("Test"); emp.setLastName("User");
        emp.setEmail("test.user.status@company.com");
        emp.setHireDate(new Date());
        emp.setSalary(new BigDecimal("40000"));
        emp.setDepartment(dept);
        Employee saved = employeeService.createEmployee(emp);

        assertEquals(Employee.EmployeeStatus.ACTIVE, saved.getStatus());

        Employee updated = employeeService.updateStatus(saved.getId(), Employee.EmployeeStatus.ON_LEAVE);
        assertEquals(Employee.EmployeeStatus.ON_LEAVE, updated.getStatus());
    }
}
