package com.example.shiftmanagement.service;

import com.example.shiftmanagement.model.MasterEmployee;
import com.example.shiftmanagement.model.EmployeeShift;
import com.example.shiftmanagement.repository.MasterEmployeeRepository;
import com.example.shiftmanagement.repository.EmployeeShiftRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MasterEmployeeService {

    @Autowired
    private MasterEmployeeRepository masterEmployeeRepository;

    @Autowired
    private EmployeeShiftRepository employeeShiftRepository;

    // Upload the master employee list (replacing old data for that month/year)
    public void uploadMasterList(String month, int year, MultipartFile file) throws IOException {
        List<MasterEmployee> employees = new ArrayList<>();

        // Read the Excel file
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // Remove old master list for the given month and year
        masterEmployeeRepository.deleteByMonthAndYear(month, year);

        // Read rows from the Excel sheet (skipping header)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String empId = row.getCell(0).getStringCellValue().trim();
            String empName = row.getCell(1).getStringCellValue().trim();
            String subDept = row.getCell(2).getStringCellValue().trim(); // Sub Department

            MasterEmployee employee = new MasterEmployee(empId, empName, subDept, month, year);
            employees.add(employee);
        }

        // Save the latest uploaded list
        masterEmployeeRepository.saveAll(employees);
    }

    // Compare shift data with the master list and return missing employees
    public List<String> findMissingEmployees(String month, int year, String department) {
        // Fetch master list for the given month and year
        List<MasterEmployee> masterList = masterEmployeeRepository.findByMonthAndYear(month, year);
        
        // Get only employees whose sub-department matches the selected department
        Set<String> masterEmployeeNames = masterList.stream()
            .filter(emp -> emp.getSubDepartment().equalsIgnoreCase(department))
            .map(MasterEmployee::getEmployeeName)
            .collect(Collectors.toSet());

        // Fetch shift data for the given month, year, and department
        List<EmployeeShift> shiftData = employeeShiftRepository.findByMonthAndYearAndDepartment(month, year, department);
        
        // Find employees in shift data but missing in master list
        return shiftData.stream()
            .map(shift -> shift.getEmployee().getEmployeeName())
            .filter(name -> !masterEmployeeNames.contains(name)) // Missing employees
            .distinct()
            .collect(Collectors.toList());
    }
}
