package com.example.shiftmanagement.repository;

import com.example.shiftmanagement.model.LockedEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface LockedEditRepository extends JpaRepository<LockedEdit, Long> {
    boolean existsByDepartmentAndMonthAndYear(String department, String month, int year);

    @Transactional
    void deleteByDepartmentAndMonthAndYear(String department, String month, int year);
}
