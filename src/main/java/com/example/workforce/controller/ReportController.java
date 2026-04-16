package com.example.workforce.controller;

import com.example.workforce.model.Employee;
import com.example.workforce.model.Project;
import com.example.workforce.service.DepartmentService;
import com.example.workforce.service.EmployeeService;
import com.example.workforce.service.ProjectService;
import com.example.workforce.service.TimeEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final EmployeeService employeeService;
    private final ProjectService projectService;
    private final DepartmentService departmentService;
    private final TimeEntryService timeEntryService;

    @Autowired
    public ReportController(EmployeeService employeeService,
                             ProjectService projectService,
                             DepartmentService departmentService,
                             TimeEntryService timeEntryService) {
        this.employeeService = employeeService;
        this.projectService = projectService;
        this.departmentService = departmentService;
        this.timeEntryService = timeEntryService;
    }

    @GetMapping("/headcount")
    public ResponseEntity<Map<String, Object>> getHeadcountReport() {
        List<Employee> all = employeeService.getAllEmployees();
        Map<String, Long> byStatus = new HashMap<>();
        for (Employee e : all) {
            String key = e.getStatus().name();
            byStatus.put(key, byStatus.getOrDefault(key, 0L) + 1);
        }
        Map<String, Long> byDepartment = new HashMap<>();
        for (Employee e : all) {
            if (e.getDepartment() != null) {
                String deptName = e.getDepartment().getName();
                byDepartment.put(deptName, byDepartment.getOrDefault(deptName, 0L) + 1);
            }
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("total", all.size());
        report.put("byStatus", byStatus);
        report.put("byDepartment", byDepartment);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/project-summary")
    public ResponseEntity<Map<String, Object>> getProjectSummary() {
        List<Project> all = projectService.getAllProjects();
        Map<String, Long> byStatus = new HashMap<>();
        for (Project p : all) {
            String key = p.getStatus().name();
            byStatus.put(key, byStatus.getOrDefault(key, 0L) + 1);
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("total", all.size());
        report.put("byStatus", byStatus);
        report.put("overdueCount", projectService.getOverdueProjects().size());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/employee/{employeeId}/utilization")
    public ResponseEntity<Map<String, Object>> getEmployeeUtilization(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date end) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        int hours = timeEntryService.getEmployeeHoursInRange(employeeId, start, end);
        List<Project> projects = projectService.getProjectsForEmployee(employeeId);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("employeeId", employeeId);
        report.put("employeeName", employee.getFullName());
        report.put("totalHoursLogged", hours);
        report.put("activeProjectCount", projects.size());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/department/{departmentId}/summary")
    public ResponseEntity<Map<String, Object>> getDepartmentSummary(@PathVariable Long departmentId) {
        List<Employee> employees = employeeService.getEmployeesByDepartment(departmentId);
        long active = 0;
        long onLeave = 0;
        for (Employee e : employees) {
            if (e.getStatus() == Employee.EmployeeStatus.ACTIVE) active++;
            if (e.getStatus() == Employee.EmployeeStatus.ON_LEAVE) onLeave++;
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("departmentId", departmentId);
        report.put("totalEmployees", employees.size());
        report.put("activeEmployees", active);
        report.put("onLeave", onLeave);
        return ResponseEntity.ok(report);
    }
}
