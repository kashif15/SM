package com.example.shiftmanagement.model;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

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
    private double onCallCount;
    private double workOffCount;
    private String month;
    private int year;
    private String holiday;
    private int plannedLeave;
    private int unplannedLeave;
    private int general;
    private int workingDays;
    private String allowanceBillable;
    private String comment;

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
    private Map<String, Double> onCallShifts = new HashMap<>();
    
    @ElementCollection
    @CollectionTable(name = "employee_work_off_shifts", joinColumns = @JoinColumn(name = "employee_shift_id"))
    @MapKeyColumn(name = "date")
    @Column(name = "shift_type")
    private Map<String, Double> workOffShifts = new HashMap<>();
    
    @ElementCollection
    @CollectionTable(name = "employee_planned_leaves", joinColumns = @JoinColumn(name = "employee_shift_id"))
    @Column(name = "leave_date")
    private Set<String> plannedLeaveDates = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "employee_unplanned_leaves", joinColumns = @JoinColumn(name = "employee_shift_id"))
    @Column(name = "leave_date")
    private Set<String> unplannedLeaveDates = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "employee_sub_restdays", joinColumns = @JoinColumn(name = "employee_shift_id"))
    @Column(name = "leave_date")
    private Set<String> subRestDays = new HashSet<>();
    
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
    
    public double getOnCallCount() {
        return onCallCount;
    }

    public void setOnCallCount(double onCallCount) {
        this.onCallCount = onCallCount;
    }

    public Map<String, Double> getOnCallShifts() {
        return onCallShifts;
    }

    public void setOnCallShifts(Map<String, Double> onCallShifts) {
        this.onCallShifts = onCallShifts;
    }
    
    public double getWorkOffCount() {
        return workOffCount;
    }

    public void setWorkOffCount(double workOffCount) {
        this.workOffCount = workOffCount;
    }
    
    public Map<String, Double> getWorkOffShifts() {
        return workOffShifts;
    }

    public void setWorkOffShifts(Map<String, Double> workOffShifts) {
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

	public String getHoliday() {
		return holiday;
	}

	public void setHoliday(String holiday) {
		this.holiday = holiday;
	}

	public int getPlannedLeave() {
		return plannedLeave;
	}

	public void setPlannedLeave(int plannedLeave) {
		this.plannedLeave = plannedLeave;
	}

	public int getUnplannedLeave() {
		return unplannedLeave;
	}

	public void setUnplannedLeave(int unplannedLeave) {
		this.unplannedLeave = unplannedLeave;
	}

	public int getGeneral() {
		return general;
	}

	public void setGeneral(int general) {
		this.general = general;
	}

	public int getWorkingDays() {
		return workingDays;
	}

	public void setWorkingDays(int workingDays) {
		this.workingDays = workingDays;
	}

	public String getAllowanceBillable() {
		return allowanceBillable;
	}

	public void setAllowanceBillable(String allowanceBillable) {
		this.allowanceBillable = allowanceBillable;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Set<String> getPlannedLeaveDates() {
		return plannedLeaveDates;
	}

	public void setPlannedLeaveDates(Set<String> plannedLeaveDates) {
		this.plannedLeaveDates = plannedLeaveDates;
	}

	public Set<String> getUnplannedLeaveDates() {
		return unplannedLeaveDates;
	}

	public void setUnplannedLeaveDates(Set<String> unplannedLeaveDates) {
		this.unplannedLeaveDates = unplannedLeaveDates;
	}

	public Set<String> getSubRestDays() {
		return subRestDays;
	}

	public void setSubRestDays(Set<String> subRestDays) {
		this.subRestDays = subRestDays;
	}
    
    
}
