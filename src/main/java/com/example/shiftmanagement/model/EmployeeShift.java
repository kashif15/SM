package com.example.shiftmanagement.model;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
public class EmployeeShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int morningShiftCount;
    private int afternoonShiftCount;
    private int nightShiftCount;
    private double totalMoney;
    private int sundayCount;
    private int onCallCount;
    private int workOffCount;
    private String month;
    private int year;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ElementCollection
    @CollectionTable(name = "employee_sunday_shifts", joinColumns = @JoinColumn(name = "employee_shift_id"))
    @MapKeyColumn(name = "date")
    @Column(name = "shift_type")
    private Map<String, String> sundayShifts = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "employee_on_call_shifts", joinColumns = @JoinColumn(name = "employee_shift_id"))  // New table for on-call shifts
    @MapKeyColumn(name = "date")
    @Column(name = "shift_type")
    private Map<String, String> onCallShifts = new HashMap<>();
    
    @ElementCollection
    @CollectionTable(name = "employee_work_off_shifts", joinColumns = @JoinColumn(name = "employee_shift_id"))
    @MapKeyColumn(name = "date")
    @Column(name = "shift_type")
    private Map<String, String> workOffShifts = new HashMap<>();
    
    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMorningShiftCount() {
        return morningShiftCount;
    }

    public void setMorningShiftCount(int morningShiftCount) {
        this.morningShiftCount = morningShiftCount;
    }

    public int getAfternoonShiftCount() {
        return afternoonShiftCount;
    }

    public void setAfternoonShiftCount(int afternoonShiftCount) {
        this.afternoonShiftCount = afternoonShiftCount;
    }

    public int getNightShiftCount() {
        return nightShiftCount;
    }

    public void setNightShiftCount(int nightShiftCount) {
        this.nightShiftCount = nightShiftCount;
    }

    public double getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(double totalMoney) {
        this.totalMoney = totalMoney;
    }

    public int getSundayCount() {
        return sundayCount;
    }

    public void setSundayCount(int sundayCount) {
        this.sundayCount = sundayCount;
    }

    public Map<String, String> getSundayShifts() {
        return sundayShifts;
    }

    public void setSundayShifts(Map<String, String> sundayShifts) {
        this.sundayShifts = sundayShifts;
    }
    
    public int getOnCallCount() {
        return onCallCount;
    }

    public void setOnCallCount(int onCallCount) {
        this.onCallCount = onCallCount;
    }

    public Map<String, String> getOnCallShifts() {
        return onCallShifts;
    }

    public void setOnCallShifts(Map<String, String> onCallShifts) {
        this.onCallShifts = onCallShifts;
    }
    
    public int getWorkOffCount() {
        return workOffCount;
    }

    public void setWorkOffCount(int workOffCount) {
        this.workOffCount = workOffCount;
    }
    
    public Map<String, String> getWorkOffShifts() {
        return workOffShifts;
    }

    public void setWorkOffShifts(Map<String, String> workOffShifts) {
        this.workOffShifts = workOffShifts;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    
    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
