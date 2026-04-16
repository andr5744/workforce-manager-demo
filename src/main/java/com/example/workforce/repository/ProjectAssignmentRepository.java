package com.example.workforce.repository;

import com.example.workforce.model.ProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {

    List<ProjectAssignment> findByProjectId(Long projectId);

    List<ProjectAssignment> findByEmployeeId(Long employeeId);

    Optional<ProjectAssignment> findByEmployeeIdAndProjectId(Long employeeId, Long projectId);

    boolean existsByEmployeeIdAndProjectId(Long employeeId, Long projectId);
}
