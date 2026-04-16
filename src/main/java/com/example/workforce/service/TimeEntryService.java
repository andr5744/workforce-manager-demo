package com.example.workforce.service;

import com.example.workforce.exception.ResourceNotFoundException;
import com.example.workforce.model.Employee;
import com.example.workforce.model.Project;
import com.example.workforce.model.TimeEntry;
import com.example.workforce.repository.EmployeeRepository;
import com.example.workforce.repository.ProjectRepository;
import com.example.workforce.repository.TimeEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    @Autowired
    public TimeEntryService(TimeEntryRepository timeEntryRepository,
                            EmployeeRepository employeeRepository,
                            ProjectRepository projectRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<TimeEntry> getAllTimeEntries() {
        return timeEntryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TimeEntry getTimeEntryById(Long id) {
        return timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeEntry", "id", id));
    }

    public TimeEntry createTimeEntry(Long employeeId, Long projectId, TimeEntry timeEntry) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (project.getStatus() == Project.ProjectStatus.COMPLETED ||
                project.getStatus() == Project.ProjectStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot log time against a completed or cancelled project");
        }
        if (employee.getStatus() == Employee.EmployeeStatus.TERMINATED) {
            throw new IllegalArgumentException(
                    "Cannot log time for a terminated employee");
        }

        timeEntry.setEmployee(employee);
        timeEntry.setProject(project);
        return timeEntryRepository.save(timeEntry);
    }

    public TimeEntry updateTimeEntry(Long id, TimeEntry timeEntryDetails) {
        TimeEntry timeEntry = getTimeEntryById(id);
        timeEntry.setDate(timeEntryDetails.getDate());
        timeEntry.setHoursWorked(timeEntryDetails.getHoursWorked());
        timeEntry.setDescription(timeEntryDetails.getDescription());
        timeEntry.setBillableStatus(timeEntryDetails.getBillableStatus());
        return timeEntryRepository.save(timeEntry);
    }

    public void deleteTimeEntry(Long id) {
        TimeEntry timeEntry = getTimeEntryById(id);
        timeEntryRepository.delete(timeEntry);
    }

    @Transactional(readOnly = true)
    public List<TimeEntry> getTimeEntriesForEmployee(Long employeeId) {
        return timeEntryRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<TimeEntry> getTimeEntriesForProject(Long projectId) {
        return timeEntryRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<TimeEntry> getTimeEntriesInRange(Date start, Date end) {
        return timeEntryRepository.findByDateBetween(start, end);
    }

    @Transactional(readOnly = true)
    public Integer getTotalHoursForProject(Long projectId) {
        Integer hours = timeEntryRepository.sumHoursForProject(projectId);
        return hours != null ? hours : 0;
    }

    @Transactional(readOnly = true)
    public Integer getEmployeeHoursInRange(Long employeeId, Date start, Date end) {
        Integer hours = timeEntryRepository.sumHoursForEmployeeInRange(employeeId, start, end);
        return hours != null ? hours : 0;
    }
}
