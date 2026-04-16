package com.example.workforce.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void headcountReport_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/reports/headcount"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void headcountReport_employeeRole_returns403() throws Exception {
        mockMvc.perform(get("/api/reports/headcount"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void headcountReport_adminRole_returnsExpectedStructure() throws Exception {
        mockMvc.perform(get("/api/reports/headcount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.byStatus").isMap())
                .andExpect(jsonPath("$.byDepartment").isMap());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void headcountReport_managerRole_returnsOk() throws Exception {
        mockMvc.perform(get("/api/reports/headcount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void projectSummaryReport_returnsExpectedStructure() throws Exception {
        mockMvc.perform(get("/api/reports/project-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.byStatus").isMap())
                .andExpect(jsonPath("$.overdueCount").isNumber());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void employeeUtilization_withDateRange_returnsOk() throws Exception {
        // Use employee ID 1 (Alice Johnson from seed data) with a wide date range
        mockMvc.perform(get("/api/reports/employee/1/utilization")
                        .param("start", "2020-01-01")
                        .param("end", "2030-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.totalHoursLogged").isNumber())
                .andExpect(jsonPath("$.activeProjectCount").isNumber());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void departmentSummary_existingDepartment_returnsOk() throws Exception {
        // Use department ID 1 (Engineering from seed data)
        mockMvc.perform(get("/api/reports/department/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentId").value(1))
                .andExpect(jsonPath("$.totalEmployees").isNumber())
                .andExpect(jsonPath("$.activeEmployees").isNumber());
    }
}
