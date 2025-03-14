package com.example.shiftmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.shiftmanagement.model.LockedShift;

import jakarta.transaction.Transactional;

@Repository
public interface LockedShiftRepository extends JpaRepository<LockedShift, Long> {
    boolean existsByDepartmentAndMonthAndYear(String department, String month, int year);
    
    @Transactional
    void deleteByDepartmentAndMonthAndYear(String department, String month, int year);

}
