package com.example.shiftmanagement.service;

import com.example.shiftmanagement.model.EmployeeShift;
import com.example.shiftmanagement.repository.EmployeeShiftRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    @Autowired
    private EmployeeShiftRepository employeeShiftRepository;

    public byte[] generateExcelReport(String month, int year, String departmentName) throws IOException {
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findByMonthAndYearAndDepartment(month, year, departmentName);

        Workbook workbook = new XSSFWorkbook();

        // Create header style with yellow background
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Oncall allowance sheet
        Sheet oncallAllowanceSheet = workbook.createSheet("Oncall allowance days");
        createOncallAllowanceSheetHeader(oncallAllowanceSheet, headerStyle);
        populateOncallAllowanceSheetData(oncallAllowanceSheet, employeeShifts);
        autoResizeColumns(oncallAllowanceSheet, 6);

        // Work off sheet
        Sheet workOffSheet = workbook.createSheet("Work off");
        createWorkOffSheetHeader(workOffSheet, headerStyle);
        populateWorkOffSheetData(workOffSheet, employeeShifts);
        autoResizeColumns(workOffSheet, 6);

        // Rest day sheet
        Sheet restDaySheet = workbook.createSheet("Rest Day");
        createRestDaySheetHeader(restDaySheet, headerStyle);
        populateRestDaySheetData(restDaySheet, employeeShifts);
        autoResizeColumns(restDaySheet, 7);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();

        return byteArrayOutputStream.toByteArray();
    }

    // Create header style with yellow background
    private CellStyle createHeaderStyle(Workbook workbook) {
        XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        return headerStyle;
    }

    private void createOncallAllowanceSheetHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        String[] headers = {
                "EMP ID", "EMPLOYEE NAME", "PROJECT/SUB DEPT",
                "ALLOWANCE BILLABLE TO DLV YES/NO", "NO OF ONCALL DAYS", "ONCALL DATES"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createWorkOffSheetHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        String[] headers = {
                "EMP ID", "EMPLOYEE NAME", "PROJECT/SUB DEPT",
                "ALLOWANCE BILLABLE TO DLV YES/NO", "WORK OFF DAYS", "WORKOFF DATES"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createRestDaySheetHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        String[] headers = {
                "EMP ID", "EMPLOYEE NAME", "PROJECT/SUB DEPT",
                "ALLOWANCE BILLABLE TO DLV YES/NO", "REST DAY", "REST DATES", "SUBSTITUTE REST DAY"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void autoResizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void populateOncallAllowanceSheetData(Sheet sheet, List<EmployeeShift> employeeShifts) {
        int rowNum = 1;
        for (EmployeeShift shift : employeeShifts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(shift.getEmployee().getEmployeeId());
            row.createCell(1).setCellValue(shift.getEmployee().getEmployeeName());
            row.createCell(2).setCellValue(shift.getDepartment().getDepartmentName());
            row.createCell(3).setCellValue(shift.getAllowanceBillable());
            row.createCell(4).setCellValue(shift.getOnCallCount());
            row.createCell(5).setCellValue(String.join(", ", shift.getOnCallShifts().keySet()));
        }
    }

    private void populateWorkOffSheetData(Sheet sheet, List<EmployeeShift> employeeShifts) {
        int rowNum = 1;
        for (EmployeeShift shift : employeeShifts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(shift.getEmployee().getEmployeeId());
            row.createCell(1).setCellValue(shift.getEmployee().getEmployeeName());
            row.createCell(2).setCellValue(shift.getDepartment().getDepartmentName());
            row.createCell(3).setCellValue(shift.getAllowanceBillable());
            row.createCell(4).setCellValue(shift.getWorkOffCount());
            row.createCell(5).setCellValue(String.join(", ", shift.getWorkOffShifts().keySet()));
        }
    }

    private void populateRestDaySheetData(Sheet sheet, List<EmployeeShift> employeeShifts) {
        int rowNum = 1;
        for (EmployeeShift shift : employeeShifts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(shift.getEmployee().getEmployeeId());
            row.createCell(1).setCellValue(shift.getEmployee().getEmployeeName());
            row.createCell(2).setCellValue(shift.getDepartment().getDepartmentName());
            row.createCell(3).setCellValue(shift.getAllowanceBillable());
            row.createCell(4).setCellValue(shift.getSundayCount());
            row.createCell(5).setCellValue(String.join(", ", shift.getSundayShifts().keySet()));
            row.createCell(6).setCellValue(String.join(", ", shift.getSubRestDays()));
        }
    }
}
