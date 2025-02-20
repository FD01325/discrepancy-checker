package com.mastercard.timesheet.discrepancy_checker.parser;

import com.mastercard.timesheet.discrepancy_checker.model.BeelineTimesheetEntry;
import com.mastercard.timesheet.discrepancy_checker.model.PrismTimesheetEntry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Component
public class ExcelParser {

    /**
     * Load Employee Mapping from "EmployeeMapping.xlsx".
     * @param file The Excel file containing Employee Mapping.
     * @return Map<ResourceName, FD_EID/MC_EID>
     */
    public static Map<String, String> loadEmployeeMapping(File file) throws Exception {
        Map<String, String> employeeMap = new HashMap<>();
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis);

        // Read "FFS List" sheet (FD EID)
        Sheet ffsSheet = workbook.getSheet("FFS List");
        if (ffsSheet == null) {
            throw new RuntimeException("FFS List sheet not found in EmployeeMapping.xlsx");
        }

        Map<String, String> fdEidMap = new HashMap<>();
        Iterator<Row> rowIterator = ffsSheet.iterator();
        rowIterator.next(); // Skip header row

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String resourceName = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().trim();
            String fdEid = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().trim();
            if (!resourceName.isEmpty() && !fdEid.isEmpty()) {
                fdEidMap.put(resourceName, fdEid);
            }
        }

        // Read "MC List" sheet (MC EID)
        Sheet mcSheet = workbook.getSheet("MC List");
        if (mcSheet == null) {
            throw new RuntimeException("MC List sheet not found in EmployeeMapping.xlsx");
        }

        rowIterator = mcSheet.iterator();
        rowIterator.next(); // Skip header row

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String resourceName = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().trim();
            String mcEid = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().trim();

            if (!resourceName.isEmpty() && !mcEid.isEmpty() && fdEidMap.containsKey(resourceName)) {
                String fdEid = fdEidMap.get(resourceName);
                employeeMap.put(resourceName, mcEid); // Map ResourceName → MC EID
                employeeMap.put(fdEid, mcEid); // Map FD EID → MC EID for easy lookup
            }
        }

        workbook.close();
        return employeeMap;
    }

    /**
     * Parses the Prism Timesheet to extract EmployeeData.
     *
     * @param prismFile MultipartFile representing the Prism Timesheet.
     * @return List of EmployeeData from the Prism Timesheet.
     */
    public List<PrismTimesheetEntry> parsePrismTimesheet(MultipartFile prismFile) throws IOException {
        List<PrismTimesheetEntry> employeeDataList = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(prismFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String fdId = getCellValue(row.getCell(5));  // Column F for User Employee ID
            String timesheetDate = getCellValue(row.getCell(2));  // Column C for Timesheet Date
            String employeeName =  getCellValue(row.getCell(3));  // Column D for Employee Name
            String typeOfHours = getCellValue(row.getCell(17)); // Column R for Type of Hours
            double totalHours = parseDouble(getCellValue(row.getCell(18))); // Column S for Total Hours

            if (!fdId.isEmpty()) {
                employeeDataList.add(new PrismTimesheetEntry(fdId, timesheetDate, employeeName, typeOfHours, totalHours));
            }
        }
        workbook.close();
        return employeeDataList;
    }

    /**
     * Parses the Beeline Timesheet to extract EmployeeData.
     *
     * @param beelineFile MultipartFile representing the Beeline Timesheet.
     * @return List of EmployeeData from the Beeline Timesheet.
     */
    public List<BeelineTimesheetEntry> parseBeelineTimesheet(MultipartFile beelineFile) throws IOException {
        List<BeelineTimesheetEntry> employeeDataList = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(beelineFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String mcId = normalizeMcId(getCellValue(row.getCell(8))); // Column I for MC ID
            String employeeName = getCellValue(row.getCell(1));  // Column B for Employee Name
            double totalUnits = parseDouble(getCellValue(row.getCell(6))); // Column G for Units

            if (!mcId.isEmpty()) {
                employeeDataList.add(new BeelineTimesheetEntry(mcId, employeeName, totalUnits));
            }
        }
        workbook.close();
        return employeeDataList;
    }

    /**
     * Helper method to normalize the MC ID by converting the first character to lowercase.
     * Example: "E12345" becomes "e12345".
     */
    private String normalizeMcId(String mcId) {
        if (mcId != null && !mcId.isEmpty()) {
            return "e" + mcId.substring(1).toLowerCase();  // Ensures all MC IDs start with 'e'
        }
        return mcId;
    }

    /**
     * Utility method to get the string value of a cell.
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    /**
     * Utility method to safely parse a double from a string.
     */
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public Map<String, Pair<String, String>> parseEmployeeMapping(MultipartFile employeeMappingFile) throws IOException {
        Map<String, Pair<String, String>> employeeMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(String.valueOf(employeeMappingFile));
             Workbook workbook = new XSSFWorkbook(fis)) {
            // Parse FFS List
            Sheet ffsSheet = workbook.getSheet("FFS List");
            Map<String, String> fdEidMap = new HashMap<>();
            for (int i = 1; i <= ffsSheet.getLastRowNum(); i++) {
                Row row = ffsSheet.getRow(i);
                if (row != null) {
                    String resourceName = row.getCell(1).getStringCellValue().trim();
                    String fdEid = row.getCell(2).getStringCellValue().trim();
                    fdEidMap.put(resourceName, fdEid);
                }
            }

            // Parse MC List
            Sheet mcSheet = workbook.getSheet("MC List");
            for (int i = 1; i <= mcSheet.getLastRowNum(); i++) {
                Row row = mcSheet.getRow(i);
                if (row != null) {
                    String resourceName = row.getCell(1).getStringCellValue().trim();
                    String mcEid = row.getCell(2).getStringCellValue().trim();
                    String fdEid = fdEidMap.get(resourceName);
                    if (fdEid != null) {
                        employeeMap.put(resourceName, new ImmutablePair<>(fdEid, mcEid));
                    }
                }
            }
        }
        return employeeMap;
    }

}
