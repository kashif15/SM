package com.example.shiftmanagement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "locked_shifts")
public class LockedShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String department;
    private String month;
    private int year;

    public LockedShift() {}

    public LockedShift(String department, String month, int year) {
        this.department = department;
        this.month = month;
        this.year = year;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
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

    
}

