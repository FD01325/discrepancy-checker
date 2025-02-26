package com.mastercard.timesheet.discrepancy_checker.parser;

import com.mastercard.timesheet.discrepancy_checker.model.BeelineTimesheetEntry;
import com.mastercard.timesheet.discrepancy_checker.model.Discrepancy;
import com.mastercard.timesheet.discrepancy_checker.model.PrismTimesheetEntry;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Component
public class ExcelParser {

    private static final String EMPLOYEE_MAPPING_FILE_PATH = "src/main/resources/EmployeeMapping.xlsx";

    /**
     * Load Employee Mapping from "EmployeeMapping.xlsx".
     * @return Map<ResourceName, FD_EID/MC_EID>
     */
    public static Map<String, String> loadEmployeeMapping() throws Exception {
        Map<String, String> employeeMap = new HashMap<>();
        FileInputStream fis = new FileInputStream(EMPLOYEE_MAPPING_FILE_PATH);
        Workbook workbook = new XSSFWorkbook(fis);

        Sheet mappingSheet = workbook.getSheet("Sheet1");

        for (int i = 1; i <= mappingSheet.getLastRowNum(); i++) {
            Row row = mappingSheet.getRow(i);

            // Skip empty rows
            if (row == null) continue;

            // Assuming the first column (index 0) is the key and the second column (index 1) is the value
            Cell keyCell = row.getCell(2);
            Cell valueCell = row.getCell(3);

            // If both key and value cells are not null, add them to the map
            if (keyCell != null && valueCell != null) {
                String key = keyCell.getStringCellValue();
                String value = valueCell.getStringCellValue();

                // Add to the Map
                employeeMap.put(key, value);
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
    public Map<String, MultiValuedMap<String, PrismTimesheetEntry>> parsePrismTimesheet(MultipartFile prismFile) throws IOException {
//        List<PrismTimesheetEntry> employeeDataList = new ArrayList<>();

        Map<String, MultiValuedMap<String, PrismTimesheetEntry>> prismEmployeeData = new HashMap<>();
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

//            if (!fdId.isEmpty()) {
//                employeeDataList.add(new PrismTimesheetEntry(fdId, timesheetDate, employeeName, typeOfHours, totalHours));
//            }
            PrismTimesheetEntry prismTimesheetEntry = new PrismTimesheetEntry(fdId, timesheetDate, employeeName, typeOfHours, totalHours);
            if(prismEmployeeData.containsKey(fdId)) {
                MultiValuedMap<String, PrismTimesheetEntry> timesheetData = prismEmployeeData.get(fdId);
                timesheetData.put(timesheetDate, prismTimesheetEntry);
                prismEmployeeData.put(fdId, timesheetData);
            } else {
                MultiValuedMap<String, PrismTimesheetEntry> timesheetData = new ArrayListValuedHashMap<>();
                timesheetData.put(timesheetDate, prismTimesheetEntry);
                prismEmployeeData.put(fdId, timesheetData);
            }
        }
        workbook.close();
        return prismEmployeeData;
    }

    /**
     * Parses the Beeline Timesheet to extract EmployeeData.
     *
     * @param beelineFile MultipartFile representing the Beeline Timesheet.
     * @return List of EmployeeData from the Beeline Timesheet.
     */
    public Map<String, Map<String, BeelineTimesheetEntry>> parseBeelineTimesheet(MultipartFile beelineFile) throws IOException {
        Map<String, Map<String, BeelineTimesheetEntry>> beelineEmployeeData = new HashMap<>();
        Workbook workbook = WorkbookFactory.create(beelineFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String mcId = normalizeMcId(getCellValue(row.getCell(8))); // Column I for MC ID
            String timesheetDate = getCellValue(row.getCell(9));  // Column J for Timesheet Date
            String employeeName = getCellValue(row.getCell(1));  // Column B for Employee Name
            double totalUnits = parseDouble(getCellValue(row.getCell(6))); // Column G for Units

//            if (!mcId.isEmpty()) {
//                employeeDataList.add(new BeelineTimesheetEntry(mcId, employeeName, totalUnits));
//            }
            BeelineTimesheetEntry beelineTimesheetEntry = new BeelineTimesheetEntry(mcId, employeeName, timesheetDate, totalUnits);
            if(beelineEmployeeData.containsKey(mcId)) {
                Map<String, BeelineTimesheetEntry> timesheetData = beelineEmployeeData.get(mcId);
                timesheetData.put(timesheetDate, beelineTimesheetEntry);
                beelineEmployeeData.put(mcId, timesheetData);
            } else {
                Map<String, BeelineTimesheetEntry> timesheetData = new HashMap<>();
                timesheetData.put(timesheetDate, beelineTimesheetEntry);
                beelineEmployeeData.put(mcId, timesheetData);
            }
        }
        workbook.close();
        return beelineEmployeeData;
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

    public byte[] exportToExcel(List<Discrepancy> discrepancies) throws IOException {
        String filePath = "discrepancies_" + System.currentTimeMillis() + ".xlsx";

        // Create Workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Discrepancies");

        // Create header row (manually setting the header)
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Sr No");
        headerRow.createCell(1).setCellValue("Resource Name");
        headerRow.createCell(2).setCellValue("FD ID");
        headerRow.createCell(3).setCellValue("MC ID");
        headerRow.createCell(4).setCellValue("Timesheet Date");
        headerRow.createCell(5).setCellValue("Discrepancy Reason");

        // Write data rows (manually setting each field for Discrepancy)
        int rowNum = 1;
        for (Discrepancy discrepancy : discrepancies) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(discrepancy.getSrNo());
            row.createCell(1).setCellValue(discrepancy.getResourceName());
            row.createCell(2).setCellValue(discrepancy.getFdId());
            row.createCell(3).setCellValue(discrepancy.getMcId());
            row.createCell(4).setCellValue(discrepancy.getTimesheetDate());
            row.createCell(5).setCellValue(discrepancy.getDiscrepancyReason());
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }

        // Read the file content into a byte array
        File file = new File(filePath);
        byte[] fileContent = Files.readAllBytes(file.toPath());

        // Delete the file after reading its contents
        deleteFile(file);

        // Return the byte array
        return fileContent;
    }

    // Helper method to delete the file
    private void deleteFile(File file) {
        if (file.exists()) {
            boolean isDeleted = file.delete();
            if (!isDeleted) {
                System.err.println("Failed to delete the file: " + file.getName());
            }
        }
    }

}
