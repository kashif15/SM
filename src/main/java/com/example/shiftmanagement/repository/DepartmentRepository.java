package com.example.shiftmanagement.repository;

import com.example.shiftmanagement.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDepartmentName(String departmentName);  // Updated method name
}
