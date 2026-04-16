package com.example.workforce.controller;

import com.example.workforce.model.Department;
import com.example.workforce.model.Employee;
import com.example.workforce.repository.DepartmentRepository;
import com.example.workforce.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void listEmployees_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void listEmployees_employeeRole_returns403() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listEmployees_adminRole_returnsOk() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void listEmployees_managerRole_returnsOk() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_validPayload_returns201() throws Exception {
        Department dept = departmentRepository.save(
                new Department("Test Dept", "Floor 1", new BigDecimal("100000"), 20));

        Employee emp = new Employee();
        emp.setFirstName("Jane");
        emp.setLastName("Smith");
        emp.setEmail("jane.smith.test@company.com");
        emp.setJobTitle("Analyst");
        emp.setHireDate(new Date());
        emp.setSalary(new BigDecimal("65000"));
        emp.setDepartment(dept);

        String json = objectMapper.writeValueAsString(emp);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith.test@company.com"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployeeById_exists_returnsOk() throws Exception {
        Department dept = departmentRepository.save(
                new Department("Lookup Dept", "Floor 2", new BigDecimal("80000"), 10));

        Employee emp = new Employee();
        emp.setFirstName("Bob");
        emp.setLastName("Test");
        emp.setEmail("bob.test.lookup@company.com");
        emp.setHireDate(new Date());
        emp.setSalary(new BigDecimal("55000"));
        emp.setDepartment(dept);
        emp = employeeRepository.save(emp);

        mockMvc.perform(get("/api/employees/" + emp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Bob"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployeeById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/employees/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchEmployees_byName_returnsMatches() throws Exception {
        // "Alice" is seeded by DataInitializer
        mockMvc.perform(get("/api/employees/search").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEmployeeStatus_validStatus_returnsOk() throws Exception {
        Department dept = departmentRepository.save(
                new Department("Status Dept", "Floor 3", new BigDecimal("70000"), 5));

        Employee emp = new Employee();
        emp.setFirstName("Status");
        emp.setLastName("Test");
        emp.setEmail("status.test@company.com");
        emp.setHireDate(new Date());
        emp.setSalary(new BigDecimal("45000"));
        emp.setDepartment(dept);
        emp = employeeRepository.save(emp);

        mockMvc.perform(patch("/api/employees/" + emp.getId() + "/status")
                        .param("status", "ON_LEAVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON_LEAVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEmployee_exists_returns204() throws Exception {
        Department dept = departmentRepository.save(
                new Department("Delete Dept", "Floor 4", new BigDecimal("50000"), 5));

        Employee emp = new Employee();
        emp.setFirstName("Delete");
        emp.setLastName("Me");
        emp.setEmail("delete.me@company.com");
        emp.setHireDate(new Date());
        emp.setSalary(new BigDecimal("40000"));
        emp.setDepartment(dept);
        emp = employeeRepository.save(emp);

        mockMvc.perform(delete("/api/employees/" + emp.getId()))
                .andExpect(status().isNoContent());
    }
}
