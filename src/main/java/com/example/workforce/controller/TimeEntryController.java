package com.example.workforce.controller;

import com.example.workforce.model.TimeEntry;
import com.example.workforce.service.TimeEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @Autowired
    public TimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    @GetMapping
    public ResponseEntity<List<TimeEntry>> getAllTimeEntries() {
        return ResponseEntity.ok(timeEntryService.getAllTimeEntries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeEntry> getById(@PathVariable Long id) {
        return ResponseEntity.ok(timeEntryService.getTimeEntryById(id));
    }

    @PostMapping("/employee/{employeeId}/project/{projectId}")
    public ResponseEntity<TimeEntry> createTimeEntry(
            @PathVariable Long employeeId,
            @PathVariable Long projectId,
            @Valid @RequestBody TimeEntry timeEntry) {
        TimeEntry created = timeEntryService.createTimeEntry(employeeId, projectId, timeEntry);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeEntry> updateTimeEntry(
            @PathVariable Long id,
            @Valid @RequestBody TimeEntry timeEntry) {
        return ResponseEntity.ok(timeEntryService.updateTimeEntry(id, timeEntry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeEntry(@PathVariable Long id) {
        timeEntryService.deleteTimeEntry(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<TimeEntry>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(timeEntryService.getTimeEntriesForEmployee(employeeId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TimeEntry>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(timeEntryService.getTimeEntriesForProject(projectId));
    }

    @GetMapping("/range")
    public ResponseEntity<List<TimeEntry>> getInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date end) {
        return ResponseEntity.ok(timeEntryService.getTimeEntriesInRange(start, end));
    }

    @GetMapping("/project/{projectId}/total-hours")
    public ResponseEntity<Integer> getTotalHoursForProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(timeEntryService.getTotalHoursForProject(projectId));
    }

    @GetMapping("/employee/{employeeId}/hours")
    public ResponseEntity<Integer> getEmployeeHours(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date end) {
        return ResponseEntity.ok(timeEntryService.getEmployeeHoursInRange(employeeId, start, end));
    }
}
