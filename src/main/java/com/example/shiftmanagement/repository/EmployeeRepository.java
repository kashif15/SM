package com.example.shiftmanagement.repository;

import com.example.shiftmanagement.model.Employee;
import com.example.shiftmanagement.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Existing method
    Optional<Employee> findByEmployeeNameIgnoreCase(String employeeName);
    
    // New method to find employee by name and department
    Optional<Employee> findByEmployeeNameAndDepartment(String employeeName, Department department);
}
