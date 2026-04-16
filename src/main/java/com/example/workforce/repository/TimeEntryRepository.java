package com.example.workforce.repository;

import com.example.workforce.model.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    List<TimeEntry> findByEmployeeId(Long employeeId);

    List<TimeEntry> findByProjectId(Long projectId);

    List<TimeEntry> findByDateBetween(Date start, Date end);

    List<TimeEntry> findByEmployeeIdAndDateBetween(Long employeeId, Date start, Date end);

    @Query("SELECT SUM(t.hoursWorked) FROM TimeEntry t WHERE t.project.id = :projectId")
    Integer sumHoursForProject(@Param("projectId") Long projectId);

    @Query("SELECT SUM(t.hoursWorked) FROM TimeEntry t " +
           "WHERE t.employee.id = :employeeId AND t.date BETWEEN :start AND :end")
    Integer sumHoursForEmployeeInRange(@Param("employeeId") Long employeeId,
                                       @Param("start") Date start,
                                       @Param("end") Date end);
}
