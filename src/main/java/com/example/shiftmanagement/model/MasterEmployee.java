package com.example.shiftmanagement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "master_employee")
public class MasterEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employeeId;
    private String employeeName;
    private String subDepartment; // Stores SFIT, MES, etc.
    
    private String month;
    private int year;

    // Constructors
    public MasterEmployee() {}

    public MasterEmployee(String employeeId, String employeeName, String subDepartment, String month, int year) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.subDepartment = subDepartment;
        this.month = month;
        this.year = year;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getSubDepartment() { return subDepartment; }
    public void setSubDepartment(String subDepartment) { this.subDepartment = subDepartment; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}
