package com.example.workforce.service;

import com.example.workforce.exception.ResourceNotFoundException;
import com.example.workforce.model.Department;
import com.example.workforce.model.Employee;
import com.example.workforce.model.Employee.EmployeeStatus;
import com.example.workforce.repository.DepartmentRepository;
import com.example.workforce.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email));
    }

    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new IllegalArgumentException(
                    "Employee with email '" + employee.getEmail() + "' already exists");
        }
        if (employee.getDepartment() != null && employee.getDepartment().getId() != null) {
            Department dept = departmentRepository.findById(employee.getDepartment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", employee.getDepartment().getId()));
            employee.setDepartment(dept);
        }
        if (employee.getManager() != null && employee.getManager().getId() != null) {
            Employee manager = employeeRepository.findById(employee.getManager().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee (Manager)", "id", employee.getManager().getId()));
            employee.setManager(manager);
        }
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);

        if (!employee.getEmail().equals(employeeDetails.getEmail()) &&
                employeeRepository.existsByEmail(employeeDetails.getEmail())) {
            throw new IllegalArgumentException(
                    "Email '" + employeeDetails.getEmail() + "' is already in use");
        }

        employee.setFirstName(employeeDetails.getFirstName());
        employee.setLastName(employeeDetails.getLastName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setPhoneNumber(employeeDetails.getPhoneNumber());
        employee.setSalary(employeeDetails.getSalary());
        employee.setJobTitle(employeeDetails.getJobTitle());
        employee.setStatus(employeeDetails.getStatus());

        if (employeeDetails.getDepartment() != null && employeeDetails.getDepartment().getId() != null) {
            Department dept = departmentRepository.findById(employeeDetails.getDepartment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", employeeDetails.getDepartment().getId()));
            employee.setDepartment(dept);
        }

        if (employeeDetails.getManager() != null && employeeDetails.getManager().getId() != null) {
            if (employeeDetails.getManager().getId().equals(id)) {
                throw new IllegalArgumentException("An employee cannot be their own manager");
            }
            Employee manager = employeeRepository.findById(employeeDetails.getManager().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee (Manager)", "id", employeeDetails.getManager().getId()));
            employee.setManager(manager);
        }

        return employeeRepository.save(employee);
    }

    public Employee updateStatus(Long id, EmployeeStatus status) {
        Employee employee = getEmployeeById(id);
        employee.setStatus(status);
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId);
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Employee> searchEmployees(String name) {
        return employeeRepository.searchByName(name);
    }

    @Transactional(readOnly = true)
    public List<Employee> getDirectReports(Long managerId) {
        return employeeRepository.findByManagerId(managerId);
    }
}
