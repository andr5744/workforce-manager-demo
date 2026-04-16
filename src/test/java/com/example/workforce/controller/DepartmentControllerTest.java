package com.example.workforce.controller;

import com.example.workforce.model.Department;
import com.example.workforce.repository.DepartmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void listDepartments_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void listDepartments_employeeRole_returns403() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listDepartments_adminRole_returnsOk() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void listDepartments_managerRole_returnsOk() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDepartment_validPayload_returns201() throws Exception {
        String json = objectMapper.writeValueAsString(
                new Department("QA Department", "Building D", new BigDecimal("90000"), 10));

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("QA Department"))
                .andExpect(jsonPath("$.location").value("Building D"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDepartmentById_exists_returnsOk() throws Exception {
        Department dept = departmentRepository.save(
                new Department("Lookup Dept", "Floor 5", new BigDecimal("60000"), 8));

        mockMvc.perform(get("/api/departments/" + dept.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lookup Dept"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDepartmentById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/departments/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDepartment_validPayload_returnsOk() throws Exception {
        Department dept = departmentRepository.save(
                new Department("Old Name", "Old Loc", new BigDecimal("50000"), 5));

        dept.setName("New Name");
        dept.setLocation("New Loc");
        String json = objectMapper.writeValueAsString(dept);

        mockMvc.perform(put("/api/departments/" + dept.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.location").value("New Loc"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDepartment_noEmployees_returns204() throws Exception {
        Department dept = departmentRepository.save(
                new Department("Delete Me", "Temp", new BigDecimal("10000"), 2));

        mockMvc.perform(delete("/api/departments/" + dept.getId()))
                .andExpect(status().isNoContent());
    }
}
