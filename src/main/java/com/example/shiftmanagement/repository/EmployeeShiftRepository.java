package com.example.shiftmanagement.repository;

import com.example.shiftmanagement.model.EmployeeShift;
import com.example.shiftmanagement.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {

    // Find employee names based on search string and department
    @Query("SELECT DISTINCT e.employee.employeeName FROM EmployeeShift e WHERE LOWER(e.employee.employeeName) LIKE LOWER(CONCAT('%', ?1, '%')) AND e.employee.department.departmentName = ?2")
    List<String> findByEmployeeName(String employeeName, String department);

    @Query("SELECT es FROM EmployeeShift es WHERE es.month = :month AND es.year = :year AND es.department.departmentName = :departmentName")
    List<EmployeeShift> findByMonthAndYearAndDepartment(@Param("month") String month, @Param("year") int year, @Param("departmentName") String departmentName);

    @Query("SELECT es FROM EmployeeShift es " +
           "WHERE es.month = :month AND es.year = :year " +
           "AND LOWER(es.employee.employeeName) = LOWER(:employeeName) " +
           "AND es.department.departmentName = :departmentName")
    List<EmployeeShift> findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(
        @Param("month") String month, 
        @Param("year") int year, 
        @Param("employeeName") String employeeName, 
        @Param("departmentName") String departmentName
    );

    @Query("SELECT es FROM EmployeeShift es " +
           "WHERE LOWER(es.employee.employeeName) = LOWER(:employeeName) " +
           "AND es.department.departmentName = :departmentName")
    List<EmployeeShift> findByEmployee_EmployeeNameIgnoreCaseAndDepartment_DepartmentName(
        @Param("employeeName") String employeeName, 
        @Param("departmentName") String departmentName
    );

 // Delete Sunday shifts by month, year, and department
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM employee_sunday_shifts WHERE employee_shift_id IN (SELECT id FROM employee_shift WHERE month = ?1 AND year = ?2 AND department_id = ?3)", nativeQuery = true)
    void deleteSundayShiftsByMonthYearAndDepartment(String month, int year, Long departmentId);

    // Delete on-call shifts by month, year, and department
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM employee_on_call_shifts WHERE employee_shift_id IN (SELECT id FROM employee_shift WHERE month = ?1 AND year = ?2 AND department_id = ?3)", nativeQuery = true)
    void deleteOnCallShiftsByMonthYearAndDepartment(String month, int year, Long departmentId);

    // Delete work-off shifts by month, year, and department
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM employee_work_off_shifts WHERE employee_shift_id IN (SELECT id FROM employee_shift WHERE month = ?1 AND year = ?2 AND department_id = ?3)", nativeQuery = true)
    void deleteWorkOffShiftsByMonthYearAndDepartment(String month, int year, Long departmentId);
    
    

    // Delete all shifts by month, year, and department
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM employee_shift WHERE month = ?1 AND year = ?2 AND department_id = ?3", nativeQuery = true)
    void deleteByMonthYearAndDepartment(String month, int year, Long departmentId);
    
 // Delete planned leaves by month, year, and department
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM employee_planned_leaves WHERE employee_shift_id IN (SELECT id FROM employee_shift WHERE month = ?1 AND year = ?2 AND department_id = ?3)", nativeQuery = true)
    void deletePlannedLeavesByMonthYearAndDepartment(String month, int year, Long departmentId);

    // Delete unplanned leaves by month, year, and department
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM employee_unplanned_leaves WHERE employee_shift_id IN (SELECT id FROM employee_shift WHERE month = ?1 AND year = ?2 AND department_id = ?3)", nativeQuery = true)
    void deleteUnplannedLeavesByMonthYearAndDepartment(String month, int year, Long departmentId);

    // Delete sub rest days by month, year, and department
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM employee_sub_restdays WHERE employee_shift_id IN (SELECT id FROM employee_shift WHERE month = ?1 AND year = ?2 AND department_id = ?3)", nativeQuery = true)
    void deleteSubRestDaysByMonthYearAndDepartment(String month, int year, Long departmentId);


}
