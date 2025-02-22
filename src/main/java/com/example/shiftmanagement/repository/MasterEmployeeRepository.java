package com.example.shiftmanagement.repository;

import com.example.shiftmanagement.model.MasterEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MasterEmployeeRepository extends JpaRepository<MasterEmployee, Long> {

    // Fetch master list for a specific month and year
    List<MasterEmployee> findByMonthAndYear(String month, int year);

    // Delete previous master list before uploading a new one
    @Transactional
    void deleteByMonthAndYear(String month, int year);
}
