package com.example.shiftmanagement.service;


import com.example.shiftmanagement.exception.EmployeeNotFoundException;
import com.example.shiftmanagement.model.Employee;
import com.example.shiftmanagement.model.Department;
import com.example.shiftmanagement.model.EmployeeShift;
import com.example.shiftmanagement.repository.EmployeeRepository;
import com.example.shiftmanagement.repository.DepartmentRepository;
import com.example.shiftmanagement.repository.EmployeeShiftRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmployeeShiftService {

    @Autowired
    private EmployeeShiftRepository employeeShiftRepository;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;

    private static final List<String> VALID_SHIFT_CODES = Arrays.asList("M", "A", "N");

    private static final List<String> INVALID_EMPLOYEE_NAMES = Arrays.asList(
            "G= General", "A=Afternoon", "N=Night", "M=Morning", "OC=On-Call",
            "P=Planned Leave", "H=Holiday", "HL= Half Day Leave", "U=Unplanned", "Legends",
            "C=Comp off", " ", "Shift Timings", "Morning -5 AM to 2:30PM IST",
            "Afternoon- 11:30 AM to 9 PM IST", "Night - 8 PM to 5:30 AM IST"
    );

    private static final List<String> Holiday_Date = Arrays.asList("26-Jan","1-May", "15-Aug", "2-Oct");
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d-MMM");

    @Transactional
    public void saveShiftData(String month, int year, String departmentName, MultipartFile file) {
        // Fetch the department or create a new one if it doesn't exist
        Department department = departmentRepository.findByDepartmentName(departmentName)
                .orElseGet(() -> {
                    Department newDepartment = new Department();
                    newDepartment.setDepartmentName(departmentName);
                    return departmentRepository.save(newDepartment);
                });

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            List<EmployeeShift> employeeShifts = new ArrayList<>();

         // Deleting the previous shift data for this month, year, and department
      
            // Delete related data first
            employeeShiftRepository.deletePlannedLeavesByMonthYearAndDepartment(month, year, department.getId());
            employeeShiftRepository.deleteUnplannedLeavesByMonthYearAndDepartment(month, year, department.getId());
            employeeShiftRepository.deleteSubRestDaysByMonthYearAndDepartment(month, year, department.getId());
            employeeShiftRepository.deleteSundayShiftsByMonthYearAndDepartment(month, year, department.getId());
            employeeShiftRepository.deleteOnCallShiftsByMonthYearAndDepartment(month, year, department.getId());
            employeeShiftRepository.deleteWorkOffShiftsByMonthYearAndDepartment(month, year, department.getId());
            employeeShiftRepository.deleteByMonthYearAndDepartment(month, year, department.getId());

            if (!rows.hasNext()) return; // Ensure there is at least one row
            Row datesRow = rows.next(); // Read the first row (dates)

            if (!rows.hasNext()) return; // Ensure there is a second row
            Row dayNamesRow = rows.next(); // Read the second row (day names)

            while (rows.hasNext()) {
                Row row = rows.next();
                if (row == null || row.getCell(0) == null || row.getCell(0).getCellType() != CellType.STRING) continue;
                
                // Check for empty or invalid employee name
                String employeeName = row.getCell(0).getStringCellValue().trim();
                if (employeeName.isEmpty() || INVALID_EMPLOYEE_NAMES.contains(employeeName)) continue;

                // Check if employee already exists in the database
                Employee employee = employeeRepository.findByEmployeeNameAndDepartment(employeeName, department).orElse(null);
                if (employee == null) {
                    employee = new Employee();
                    employee.setEmployeeName(employeeName);
                    employee.setDepartment(department);
                    employee = employeeRepository.save(employee);
                }

                EmployeeShift employeeShift = new EmployeeShift();
                employeeShift.setEmployee(employee);
                employeeShift.setMonth(month);
                employeeShift.setYear(year);
                employeeShift.setDepartment(department);
                int morningShiftCount = 0, afternoonShiftCount = 0, nightShiftCount = 0;
                int sundayCount = 0;
                int plannedLeave = 0;
                int unplannedLeave = 0;
                int general = 0;
                
                Map<String, String> sundayShifts = new HashMap<>();

                boolean hasValidShift = false; // Flag to check for valid shifts

                for (int i = 1; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null && cell.getCellType() == CellType.STRING) {
                        String shiftType = cell.getStringCellValue().trim();
                        
                        String dateStr = getCellStringValue(datesRow.getCell(i));
                        String dayName = getCellStringValue(dayNamesRow.getCell(i));

                        
                        boolean isWeekend = "Sat".equals(dayName) || "Sun".equals(dayName);
                        
                        if (VALID_SHIFT_CODES.contains(shiftType)) {
                            hasValidShift = true; 

                            
                            if ("Sun".equals(dayName)) {
                                sundayCount++;
                                sundayShifts.put(dateStr, shiftType);
                            }
                            
                            if(Holiday_Date.contains(dateStr)) {
                            	employeeShift.setHoliday(dateStr);
                            } else if (!isWeekend) {
                                switch (shiftType) {
                                    case "M":
                                        morningShiftCount++;
                                        break;
                                    case "A":
                                        afternoonShiftCount++;
                                        break;
                                    case "N":
                                        nightShiftCount++;
                                        break;
                                }
                            }
                        }else {
                        	
                        	switch(shiftType) {
                        	case "P":
                        		employeeShift.getPlannedLeaveDates().add(dateStr);
                        		plannedLeave++;
                        		break;
                        	case "U":
                        		employeeShift.getUnplannedLeaveDates().add(dateStr);
                        		unplannedLeave++;
                        		break;
                        	case "C":
                        		if(!isWeekend) {
                        			general++;
                        		}
                        		break;
                        	case "G":
                        		if(!isWeekend) {
                        			general++;
                        			hasValidShift = true;
                        		}
                        		break;
                        	}
                        	
                        }

                    }
                }

                // Skip rows without valid shifts
                if (!hasValidShift) continue;

                double morningRate = getRateForShift("Morning");
                double afternoonRate = getRateForShift("Afternoon");
                double nightRate = getRateForShift("Night");
                double totalMoney = (morningShiftCount * morningRate) + (afternoonShiftCount * afternoonRate) + (nightShiftCount * nightRate);
                int workingDays = morningShiftCount + afternoonShiftCount + nightShiftCount + plannedLeave + unplannedLeave + general;
                employeeShift.setMorningShiftCount(morningShiftCount);
                employeeShift.setAfternoonShiftCount(afternoonShiftCount);
                employeeShift.setNightShiftCount(nightShiftCount);
                employeeShift.setTotalMoney(totalMoney);
                employeeShift.setSundayCount(sundayCount);
                employeeShift.setSundayShifts(sundayShifts);
                employeeShift.setPlannedLeave(plannedLeave);
                employeeShift.setUnplannedLeave(unplannedLeave);
                employeeShift.setGeneral(general);
                employeeShift.setWorkingDays(workingDays);
                employeeShifts.add(employeeShift);
            }
            employeeShiftRepository.saveAll(employeeShifts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return DATE_FORMAT.format(cell.getDateCellValue());
            } else {
                return String.valueOf((int) cell.getNumericCellValue());
            }
        }
        return "";
    }

    public List<EmployeeShift> getAllEmployeeShifts(String month, int year, String department) {
        return employeeShiftRepository.findByMonthAndYearAndDepartment(month, year, department);
    }


    public EmployeeShift getEmployeeDetails(String month, int year, String employeeName, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);
        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }
        return employeeShifts.get(0);
    }


    
    public List<String> findByEmployeeName(String employeeName, String department) {
        List<String> employeeShifts = employeeShiftRepository.findByEmployeeName(employeeName, department);
        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found");
        }
        return employeeShifts;
    }
    

    @Transactional
    public List<EmployeeShift> updateEmployeeDetails(String employeeName, EmployeeShift updatedEmployeeShift, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByEmployee_EmployeeNameIgnoreCaseAndDepartment_DepartmentName(employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found in department " + departmentName);
        }

        for (EmployeeShift employeeShift : employeeShifts) {
            if (updatedEmployeeShift.getEmployee() != null) {
                Employee employee = employeeShift.getEmployee();
                String newEmployeeId = updatedEmployeeShift.getEmployee().getEmployeeId();
                String newEmail = updatedEmployeeShift.getEmployee().getEmail();
                employee.setEmployeeId(newEmployeeId);
                employee.setEmail(newEmail);
                employeeRepository.save(employee);
            }
        }
        return employeeShiftRepository.saveAll(employeeShifts);
    }


    
    @Transactional
    public void deleteEmployeeData(String employeeName, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByEmployee_EmployeeNameIgnoreCaseAndDepartment_DepartmentName(employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found in department " + departmentName);
        }

        Employee employee = employeeShifts.get(0).getEmployee();

        employeeShiftRepository.deleteAll(employeeShifts); // Delete all shifts related to the employee
        employeeRepository.delete(employee); // Delete the employee record
    }

    
    @Transactional
    public EmployeeShift updateMorningShiftCount(String employeeName, String month, int year, int morningShiftCount, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }

        EmployeeShift employeeShift = employeeShifts.get(0);
        employeeShift.setMorningShiftCount(morningShiftCount);
        recalculateTotalMoney(employeeShift);
        return employeeShiftRepository.save(employeeShift);
    }



    @Transactional
    public EmployeeShift updateAfternoonShiftCount(String employeeName, String month, int year, int afternoonShiftCount, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }

        EmployeeShift employeeShift = employeeShifts.get(0);
        employeeShift.setAfternoonShiftCount(afternoonShiftCount);
        recalculateTotalMoney(employeeShift);
        return employeeShiftRepository.save(employeeShift);
    }



    @Transactional
    public EmployeeShift updateNightShiftCount(String employeeName, String month, int year, int nightShiftCount, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }

        EmployeeShift employeeShift = employeeShifts.get(0);
        employeeShift.setNightShiftCount(nightShiftCount);
        recalculateTotalMoney(employeeShift);
        return employeeShiftRepository.save(employeeShift);
    }

    
    @Transactional
    public EmployeeShift updateSundayShift(String employeeName, String month, int year, String date, String shiftType, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }

        EmployeeShift employeeShift = employeeShifts.get(0);
        Map<String, String> sundayShifts = employeeShift.getSundayShifts();
        sundayShifts.put(date, shiftType);
        employeeShift.setSundayCount(sundayShifts.size());
        return employeeShiftRepository.save(employeeShift);
    }

    
    @Transactional
    public EmployeeShift updateOnCallShift(String employeeName, String month, int year, String date, double hoursWorked, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }

        EmployeeShift employeeShift = employeeShifts.get(0);
        Map<String, Double> onCallShifts = employeeShift.getOnCallShifts();

        // Update the on-call shift for the given date with hours worked
        onCallShifts.put(date, hoursWorked);
        
        // Recalculate the on-call count based on the hours worked
        double newOnCall = 0;
        for (Double hours : onCallShifts.values()) {
            if (hours > 2 && hours < 5) {
                newOnCall += 0.5;
            } else if (hours >= 5) {
                newOnCall += 1;
            }
        }

        employeeShift.setOnCallCount(newOnCall);

        return employeeShiftRepository.save(employeeShift);
    }


    
    @Transactional
    public EmployeeShift updateWorkOffShift(String employeeName, String month, int year, String date, double hoursWorked, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }

        EmployeeShift employeeShift = employeeShifts.get(0);
        Map<String, Double> workOffShifts = employeeShift.getWorkOffShifts();

        // Update the work-off shift for the given date with hours worked
        workOffShifts.put(date, hoursWorked);

        // Recalculate the work-off count based on the hours worked
        double newWorkOff = 0;
        for (Double hours : workOffShifts.values()) {
            if (hours > 2 && hours < 5) {
                newWorkOff += 0.5;
            } else if (hours >= 5) {
                newWorkOff += 1;
            }
        }

        employeeShift.setWorkOffCount(newWorkOff);

        return employeeShiftRepository.save(employeeShift);
    }
    
    @Transactional
    public EmployeeShift updatePlannedLeaveCount(String employeeName, String month, int year, String leaveDate, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }

        EmployeeShift employeeShift = employeeShifts.get(0); 
        employeeShift.getPlannedLeaveDates().add(leaveDate);
        employeeShift.setPlannedLeave(employeeShift.getPlannedLeaveDates().size());

        return employeeShiftRepository.save(employeeShift);
    }
    
    @Transactional
    public EmployeeShift updateUnplannedLeaveCount(String employeeName, String month, int year, String leaveDate, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }

        EmployeeShift employeeShift = employeeShifts.get(0); 
        employeeShift.getUnplannedLeaveDates().add(leaveDate);
        employeeShift.setUnplannedLeave(employeeShift.getUnplannedLeaveDates().size());

        return employeeShiftRepository.save(employeeShift);
    }
    
    @Transactional
    public EmployeeShift updateSubRestDays(String employeeName, String month, int year, String leaveDate, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }

        EmployeeShift employeeShift = employeeShifts.get(0); 
        employeeShift.getSubRestDays().add(leaveDate);

        return employeeShiftRepository.save(employeeShift);
    }
    
    @Transactional
    public EmployeeShift updateGeneralCount(String employeeName, String month, int year, int generalCount, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }

        EmployeeShift employeeShift = employeeShifts.get(0); 
        employeeShift.setGeneral(generalCount);

        return employeeShiftRepository.save(employeeShift);
    }
    
    @Transactional
    public EmployeeShift updateAllowanceBillable(String employeeName, String month, int year, String allowanceBillable, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }

        EmployeeShift employeeShift = employeeShifts.get(0); 
        employeeShift.setAllowanceBillable(allowanceBillable);

        return employeeShiftRepository.save(employeeShift);
    }
    
    @Transactional
    public EmployeeShift updateComment(String employeeName, String month, int year, String comment, String departmentName) {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthYearAndEmployee_EmployeeNameAndDepartment_DepartmentName(month, year, employeeName, departmentName);

        if (employeeShifts.isEmpty()) {
            throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found for " + month + " " + year + " in department " + departmentName);
        }

        EmployeeShift employeeShift = employeeShifts.get(0); 
        employeeShift.setComment(comment);

        return employeeShiftRepository.save(employeeShift);
    }


    
    private void recalculateTotalMoney(EmployeeShift employeeShift) {
        double morningRate = getRateForShift("Morning");
        double afternoonRate = getRateForShift("Afternoon");
        double nightRate = getRateForShift("Night");

        double totalMoney = (employeeShift.getMorningShiftCount() * morningRate)
                          + (employeeShift.getAfternoonShiftCount() * afternoonRate)
                          + (employeeShift.getNightShiftCount() * nightRate);

        employeeShift.setTotalMoney(totalMoney);
    }

    
    private double getRateForShift(String shiftType) {
        switch (shiftType) {
            case "Morning":
                return 400.0;
            case "Afternoon":
                return 500.0;
            case "Night":
                return 700.0;
            default:
                return 0.0;
        }
    }
}
