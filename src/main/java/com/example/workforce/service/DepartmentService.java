package com.example.workforce.service;

import com.example.workforce.exception.ResourceNotFoundException;
import com.example.workforce.model.Department;
import com.example.workforce.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    public Department createDepartment(Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new IllegalArgumentException(
                    "Department with name '" + department.getName() + "' already exists");
        }
        return departmentRepository.save(department);
    }

    public Department updateDepartment(Long id, Department departmentDetails) {
        Department department = getDepartmentById(id);
        if (!department.getName().equals(departmentDetails.getName()) &&
                departmentRepository.existsByName(departmentDetails.getName())) {
            throw new IllegalArgumentException(
                    "Department with name '" + departmentDetails.getName() + "' already exists");
        }
        department.setName(departmentDetails.getName());
        department.setLocation(departmentDetails.getLocation());
        department.setBudget(departmentDetails.getBudget());
        department.setHeadCountLimit(departmentDetails.getHeadCountLimit());
        return departmentRepository.save(department);
    }

    public void deleteDepartment(Long id) {
        Department department = getDepartmentById(id);
        if (!department.getEmployees().isEmpty()) {
            throw new IllegalStateException("Cannot delete a department that still has employees assigned");
        }
        departmentRepository.delete(department);
    }

    @Transactional(readOnly = true)
    public Department getDepartmentWithEmployees(Long id) {
        return departmentRepository.findByIdWithEmployees(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Department> getDepartmentsByLocation(String location) {
        return departmentRepository.findByLocationIgnoreCase(location);
    }
}
