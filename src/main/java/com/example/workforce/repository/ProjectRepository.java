package com.example.workforce.repository;

import com.example.workforce.model.Project;
import com.example.workforce.model.Project.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByClientName(String clientName);

    @Query("SELECT p FROM Project p WHERE p.endDate < :date AND p.status = 'IN_PROGRESS'")
    List<Project> findOverdueProjects(@Param("date") Date date);

    @Query("SELECT p FROM Project p JOIN p.assignments a WHERE a.employee.id = :employeeId")
    List<Project> findByEmployeeId(@Param("employeeId") Long employeeId);
}
