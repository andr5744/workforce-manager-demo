package com.example.workforce.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests verifying that Spring Security role-based access control works
 * correctly across all API endpoints. This is critical migration material —
 * SecurityConfig uses WebSecurityConfigurerAdapter + antMatchers() which
 * must become SecurityFilterChain + requestMatchers() in Spring Boot 3.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Unauthenticated access ──

    @Test
    void healthEndpoint_public_returnsOk() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void employeesEndpoint_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void departmentsEndpoint_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void projectsEndpoint_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void timeEntriesEndpoint_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/time-entries"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reportsEndpoint_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/reports/headcount"))
                .andExpect(status().isUnauthorized());
    }

    // ── EMPLOYEE role access ──

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employees_employeeRole_forbidden() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void departments_employeeRole_forbidden() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void projects_employeeRole_allowed() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void timeEntries_employeeRole_allowed() throws Exception {
        mockMvc.perform(get("/api/time-entries"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void reports_employeeRole_forbidden() throws Exception {
        mockMvc.perform(get("/api/reports/headcount"))
                .andExpect(status().isForbidden());
    }

    // ── MANAGER role access ──

    @Test
    @WithMockUser(roles = "MANAGER")
    void employees_managerRole_allowed() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void departments_managerRole_allowed() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void projects_managerRole_allowed() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void reports_managerRole_allowed() throws Exception {
        mockMvc.perform(get("/api/reports/headcount"))
                .andExpect(status().isOk());
    }

    // ── ADMIN role access ──

    @Test
    @WithMockUser(roles = "ADMIN")
    void employees_adminRole_allowed() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void departments_adminRole_allowed() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void projects_adminRole_allowed() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reports_adminRole_allowed() throws Exception {
        mockMvc.perform(get("/api/reports/headcount"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actuatorInfo_adminRole_allowed() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void actuatorInfo_managerRole_forbidden() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isForbidden());
    }
}
