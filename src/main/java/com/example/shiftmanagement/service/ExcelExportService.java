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

        // Create cell style with borders
        CellStyle borderedCellStyle = createBorderedCellStyle(workbook);

        // Oncall allowance sheet
        Sheet oncallAllowanceSheet = workbook.createSheet("Oncall allowance days");
        createOncallAllowanceSheetHeader(oncallAllowanceSheet, headerStyle);
        populateOncallAllowanceSheetData(oncallAllowanceSheet, employeeShifts, borderedCellStyle);
        autoResizeColumns(oncallAllowanceSheet, 5);

        // Work off sheet
        Sheet workOffSheet = workbook.createSheet("Work off");
        createWorkOffSheetHeader(workOffSheet, headerStyle);
        populateWorkOffSheetData(workOffSheet, employeeShifts, borderedCellStyle);
        autoResizeColumns(workOffSheet, 5);

        // Rest day sheet
        Sheet restDaySheet = workbook.createSheet("Rest Day");
        createRestDaySheetHeader(restDaySheet, headerStyle);
        populateRestDaySheetData(restDaySheet, employeeShifts, borderedCellStyle);
        autoResizeColumns(restDaySheet, 6);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();

        return byteArrayOutputStream.toByteArray();
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        return headerStyle;
    }

    private CellStyle createBorderedCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        return cellStyle;
    }

    private void createOncallAllowanceSheetHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        String[] headers = {
                "EMP ID", "EMPLOYEE NAME", "ALLOWANCE BILLABLE TO DLV YES/NO", 
                "NO OF ONCALL DAYS", "ONCALL DATES"
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
                "EMP ID", "EMPLOYEE NAME", "ALLOWANCE BILLABLE TO DLV YES/NO",
                "WORK OFF DAYS", "WORKOFF DATES"
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
                "EMP ID", "EMPLOYEE NAME", "ALLOWANCE BILLABLE TO DLV YES/NO",
                "REST DAY", "REST DATES", "SUBSTITUTE REST DAY"
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

    private void populateOncallAllowanceSheetData(Sheet sheet, List<EmployeeShift> employeeShifts, CellStyle cellStyle) {
        int rowNum = 1;
        for (EmployeeShift shift : employeeShifts) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, shift.getEmployee().getEmployeeId(), cellStyle);
            createCell(row, 1, shift.getEmployee().getEmployeeName(), cellStyle);
            createCell(row, 2, shift.getAllowanceBillable(), cellStyle);
            createCell(row, 3, shift.getOnCallCount(), cellStyle);
            createCell(row, 4, String.join(", ", shift.getOnCallShifts().keySet()), cellStyle);
        }
    }

    private void populateWorkOffSheetData(Sheet sheet, List<EmployeeShift> employeeShifts, CellStyle cellStyle) {
        int rowNum = 1;
        for (EmployeeShift shift : employeeShifts) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, shift.getEmployee().getEmployeeId(), cellStyle);
            createCell(row, 1, shift.getEmployee().getEmployeeName(), cellStyle);
            createCell(row, 2, shift.getAllowanceBillable(), cellStyle);
            createCell(row, 3, shift.getWorkOffCount(), cellStyle);
            createCell(row, 4, String.join(", ", shift.getWorkOffShifts().keySet()), cellStyle);
        }
    }

    private void populateRestDaySheetData(Sheet sheet, List<EmployeeShift> employeeShifts, CellStyle cellStyle) {
        int rowNum = 1;
        for (EmployeeShift shift : employeeShifts) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, shift.getEmployee().getEmployeeId(), cellStyle);
            createCell(row, 1, shift.getEmployee().getEmployeeName(), cellStyle);
            createCell(row, 2, shift.getAllowanceBillable(), cellStyle);
            createCell(row, 3, shift.getSundayCount(), cellStyle);
            createCell(row, 4, String.join(", ", shift.getSundayShifts().keySet()), cellStyle);
            createCell(row, 5, String.join(", ", shift.getSubRestDays()), cellStyle);
        }
    }

    private void createCell(Row row, int column, String value, CellStyle cellStyle) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
    }

    private void createCell(Row row, int column, int value, CellStyle cellStyle) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
    }

    private void createCell(Row row, int column, double value, CellStyle cellStyle) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
    }

}
