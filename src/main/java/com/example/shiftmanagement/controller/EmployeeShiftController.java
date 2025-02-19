package com.example.shiftmanagement.controller;

import com.example.shiftmanagement.model.EmployeeShift;
import com.example.shiftmanagement.service.EmployeeShiftService;
import com.example.shiftmanagement.model.LockedShift;
import com.example.shiftmanagement.service.ExcelExportService;
import com.example.shiftmanagement.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/shift")
public class EmployeeShiftController {

    @Autowired
    private EmployeeShiftService employeeShiftService;
    
    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // Helper method to validate department access
    private boolean isAuthorized(HttpServletRequest request, String department) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " prefix
            String userDepartment = jwtTokenUtil.getDepartmentFromToken(token);  // Get user's department from JWT
            return "ALL".equalsIgnoreCase(userDepartment) || userDepartment.equalsIgnoreCase(department);
        }
        return false;
    }
    
 // Helper method to check if user is a Super Admin
    private boolean isSuperAdmin(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String userDepartment = jwtTokenUtil.getDepartmentFromToken(token);
            return "ALL".equalsIgnoreCase(userDepartment); // Superadmin has department as "ALL"
        }
        return false;
    }
    
    @PostMapping("/lock")
    public ResponseEntity<String> lockDepartmentShifts(HttpServletRequest request, 
                                                       @RequestParam String department, 
                                                       @RequestParam String month, 
                                                       @RequestParam int year) {
        if (!isSuperAdmin(request)) {
            return ResponseEntity.status(403).body("Only superadmins can lock shift uploads.");
        }

        employeeShiftService.lockShiftData(department, month, year);
        return ResponseEntity.ok("Shift uploads locked successfully.");
    }




    @PostMapping("/upload")
    public ResponseEntity<String> uploadShiftData(HttpServletRequest request, @RequestParam("month") String month, @RequestParam("year") int year, @RequestParam("department") String department, @RequestParam("file") MultipartFile file) {
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).body("Access denied to this department's data");
        }
        
        if (employeeShiftService.isShiftLocked(department, month, year)) {
            return ResponseEntity.status(403).body("Shift data for this month is locked.");
        }
        
        employeeShiftService.saveShiftData(month, year, department, file);
        return ResponseEntity.ok("Shift data uploaded successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmployeeShift>> getAllEmployeeShifts(HttpServletRequest request, @RequestParam("month") String month, @RequestParam("year") int year, @RequestParam("department") String department) {
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(employeeShiftService.getAllEmployeeShifts(month, year, department));
    }

    @GetMapping("/search/{employeeName}")
    public ResponseEntity<List<String>> findByEmployeeName(HttpServletRequest request, @PathVariable String employeeName, @RequestParam("department") String department) {
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        List<String> employeeShifts = employeeShiftService.findByEmployeeName(employeeName, department);
        if (employeeShifts != null && !employeeShifts.isEmpty()) {
            return ResponseEntity.ok(employeeShifts);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/details/{employeeName}")
    public ResponseEntity<EmployeeShift> getEmployeeDetails(HttpServletRequest request, @RequestParam("month") String month, @RequestParam("year") int year, @PathVariable String employeeName, @RequestParam("department") String department) {
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        EmployeeShift employeeShift = employeeShiftService.getEmployeeDetails(month, year, employeeName, department);
        if (employeeShift != null) {
            return ResponseEntity.ok(employeeShift);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update/{employeeName}")
    public ResponseEntity<List<EmployeeShift>> updateEmployeeDetails(HttpServletRequest request, @PathVariable String employeeName, @RequestParam("department") String department, @RequestBody EmployeeShift updatedEmployeeShift) {
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        List<EmployeeShift> updatedShifts = employeeShiftService.updateEmployeeDetails(employeeName, updatedEmployeeShift, department);
        if (updatedShifts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedShifts);
    }

    @PutMapping("/update/morning/{employeeName}")
    public ResponseEntity<EmployeeShift> updateMorningShift(HttpServletRequest request, @PathVariable String employeeName, @RequestParam("department") String department, @RequestParam("month") String month, @RequestParam("year") int year, @RequestParam("count") int morningShiftCount) {
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        EmployeeShift updatedShift = employeeShiftService.updateMorningShiftCount(employeeName, month, year, morningShiftCount, department);
        return ResponseEntity.ok(updatedShift);
    }

    @PutMapping("/update/afternoon/{employeeName}")
    public ResponseEntity<EmployeeShift> updateAfternoonShift(HttpServletRequest request, @PathVariable String employeeName, @RequestParam("department") String department, @RequestParam("month") String month, @RequestParam("year") int year, @RequestParam("count") int afternoonShiftCount) {
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        EmployeeShift updatedShift = employeeShiftService.updateAfternoonShiftCount(employeeName, month, year, afternoonShiftCount, department);
        return ResponseEntity.ok(updatedShift);
    }

    @PutMapping("/update/night/{employeeName}")
    public ResponseEntity<EmployeeShift> updateNightShift(HttpServletRequest request, @PathVariable String employeeName, @RequestParam("department") String department, @RequestParam("month") String month, @RequestParam("year") int year, @RequestParam("count") int nightShiftCount) {
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        EmployeeShift updatedShift = employeeShiftService.updateNightShiftCount(employeeName, month, year, nightShiftCount, department);
        return ResponseEntity.ok(updatedShift);
    }

    @PutMapping("/update/sunday/{employeeName}")
    public ResponseEntity<EmployeeShift> updateSundayShift(HttpServletRequest request, @PathVariable String employeeName, @RequestParam("department") String department, @RequestParam("month") String month, @RequestParam("year") int year, @RequestParam("date") String date, @RequestParam("shiftType") String shiftType) {
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        EmployeeShift updatedShift = employeeShiftService.updateSundayShift(employeeName, month, year, date, shiftType, department);
        return ResponseEntity.ok(updatedShift);
    }

    @PutMapping("/update/oncall/{employeeName}")
    public ResponseEntity<EmployeeShift> updateOnCallShift(HttpServletRequest request, @PathVariable String employeeName, @RequestParam("department") String department, @RequestParam String month, @RequestParam int year, @RequestParam String date, @RequestParam double hoursWorked) {
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        EmployeeShift updatedShift = employeeShiftService.updateOnCallShift(employeeName, month, year, date, hoursWorked, department);
        return ResponseEntity.ok(updatedShift);
    }

    @PutMapping("/update/workOff/{employeeName}")
    public ResponseEntity<EmployeeShift> updateWorkOffShift(HttpServletRequest request, @PathVariable String employeeName, @RequestParam("department") String department, @RequestParam String month, @RequestParam int year, @RequestParam String date, @RequestParam double hoursWorked) {
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        } 
        EmployeeShift updatedShift = employeeShiftService.updateWorkOffShift(employeeName, month, year, date, hoursWorked, department);
        return ResponseEntity.ok(updatedShift);
    }
    
    @PutMapping("/update/pl/{employeeName}")
    public ResponseEntity<EmployeeShift> updatePlannedLeave(
            HttpServletRequest request,
            @PathVariable String employeeName,
            @RequestParam("department") String department,
            @RequestParam("month") String month,
            @RequestParam("year") int year,
            @RequestParam("date") String leaveDate) {
        
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        
        EmployeeShift updatedShift = employeeShiftService.updatePlannedLeaveCount(employeeName, month, year, leaveDate, department);
        return ResponseEntity.ok(updatedShift);
    }
    
    @PutMapping("/update/upl/{employeeName}")
    public ResponseEntity<EmployeeShift> updateUnplannedLeave(
            HttpServletRequest request,
            @PathVariable String employeeName,
            @RequestParam("department") String department,
            @RequestParam("month") String month,
            @RequestParam("year") int year,
            @RequestParam("date") String leaveDate) {
        
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        
        EmployeeShift updatedShift = employeeShiftService.updateUnplannedLeaveCount(employeeName, month, year, leaveDate, department);
        return ResponseEntity.ok(updatedShift);
    }
    
    @PutMapping("/update/subs/{employeeName}")
    public ResponseEntity<EmployeeShift> updateSubRestDays(
            HttpServletRequest request,
            @PathVariable String employeeName,
            @RequestParam("department") String department,
            @RequestParam("month") String month,
            @RequestParam("year") int year,
            @RequestParam("date") String leaveDate) {
        
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        
        EmployeeShift updatedShift = employeeShiftService.updateSubRestDays(employeeName, month, year, leaveDate, department);
        return ResponseEntity.ok(updatedShift);
    }

    @PutMapping("/update/general/{employeeName}")
    public ResponseEntity<EmployeeShift> updateGeneralShift(
            HttpServletRequest request,
            @PathVariable String employeeName,
            @RequestParam("department") String department,
            @RequestParam("month") String month,
            @RequestParam("year") int year,
            @RequestParam("count") int generalCount) {
        
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        
        EmployeeShift updatedShift = employeeShiftService.updateGeneralCount(employeeName, month, year, generalCount, department);
        return ResponseEntity.ok(updatedShift);
    }

    @PutMapping("/update/bill/{employeeName}")
    public ResponseEntity<EmployeeShift> updateAllowanceBillable(
            HttpServletRequest request,
            @PathVariable String employeeName,
            @RequestParam("department") String department,
            @RequestParam("month") String month,
            @RequestParam("year") int year,
            @RequestParam("billable") String allowanceBillable) {
        
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        
        EmployeeShift updatedShift = employeeShiftService.updateAllowanceBillable(employeeName, month, year, allowanceBillable, department);
        return ResponseEntity.ok(updatedShift);
    }

    @PutMapping("/update/comment/{employeeName}")
    public ResponseEntity<EmployeeShift> updateComment(
            HttpServletRequest request,
            @PathVariable String employeeName,
            @RequestParam("department") String department,
            @RequestParam("month") String month,
            @RequestParam("year") int year,
            @RequestParam("comment") String comment) {
        
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        
        EmployeeShift updatedShift = employeeShiftService.updateComment(employeeName, month, year, comment, department);
        return ResponseEntity.ok(updatedShift);
    }

    @DeleteMapping("/delete/{employeeName}")
    public ResponseEntity<Void> deleteEmployeeData(HttpServletRequest request, @PathVariable String employeeName, @RequestParam("department") String department) {
        if (!isAuthorized(request, department)) {
            return ResponseEntity.status(403).build();
        }
        employeeShiftService.deleteEmployeeData(employeeName, department);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExcelReport(
            HttpServletRequest request,
            @RequestParam("month") String month,
            @RequestParam("year") int year,
            @RequestParam("departmentName") String departmentName) {
        if (!isAuthorized(request, departmentName)) {
        	 return ResponseEntity.status(403).build();
        }
        try {
            byte[] excelData = excelExportService.generateExcelReport(month, year, departmentName);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=Allowance_" + departmentName + "_" + month + "_" + year + ".xlsx");
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
