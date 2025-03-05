package com.mastercard.timesheet.discrepancy_checker.utils;

import com.mastercard.timesheet.discrepancy_checker.model.BeelineTimesheetEntry;
import com.mastercard.timesheet.discrepancy_checker.model.Discrepancy;
import com.mastercard.timesheet.discrepancy_checker.model.PrismTimesheetEntry;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelParserUtils {
    /**
     * Load Employee Mapping from "EmployeeMapping.xlsx".
     *
     * @return Map<ResourceName, FD_EID / MC_EID>
     */
    public static Map<String, String> loadEmployeeMapping(String mappingFilePath) throws Exception {
        Map<String, String> employeeMap = new HashMap<>();

        File mappingFolder = new File(mappingFilePath);
        File[] mappingFiles = mappingFolder.listFiles();

        if (mappingFiles != null && mappingFiles.length == 1) {
            try (InputStream mappingInputStream = new FileInputStream(mappingFiles[0])) {
                Workbook workbook = new XSSFWorkbook(mappingInputStream);
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
                        String key = keyCell.getStringCellValue().toUpperCase();
                        String value = valueCell.getStringCellValue().toUpperCase();

                        // Add to the Map
                        employeeMap.put(key, value);
                    }
                }
                workbook.close();
            } catch (IOException e) {
                System.out.println("Exception");
            }
        }
        return employeeMap;
    }

    /**
     * Parses the Prism Timesheet to extract EmployeeData.
     *
     * @param prismInputStream MultipartFile representing the Prism Timesheet.
     * @return Map of EmployeeData from the Prism Timesheet.
     */
    public static Map<String, MultiValuedMap<String, PrismTimesheetEntry>> parsePrismTimesheet(InputStream prismInputStream) throws IOException {
        Map<String, MultiValuedMap<String, PrismTimesheetEntry>> prismEmployeeData = new HashMap<>();
        Workbook workbook = WorkbookFactory.create(prismInputStream);
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) continue;

            String fdId = getCellValue(row.getCell(5)).toUpperCase();  // Column F for User Employee ID
            String timesheetDate = getCellValue(row.getCell(2));  // Column C for Timesheet Date
            String employeeName = getCellValue(row.getCell(3));  // Column D for Employee Name
            String typeOfHours = getCellValue(row.getCell(17)); // Column R for Type of Hours
            double totalHours = parseDouble(getCellValue(row.getCell(18))); // Column S for Total Hours

            PrismTimesheetEntry prismTimesheetEntry = new PrismTimesheetEntry(fdId, timesheetDate, employeeName, typeOfHours, totalHours);
            MultiValuedMap<String, PrismTimesheetEntry> timesheetData;
            if (prismEmployeeData.containsKey(fdId)) {
                timesheetData = prismEmployeeData.get(fdId);
            } else {
                timesheetData = new ArrayListValuedHashMap<>();
            }
            timesheetData.put(timesheetDate, prismTimesheetEntry);
            prismEmployeeData.put(fdId, timesheetData);
        }
        workbook.close();
        return prismEmployeeData;
    }

    /**
     * Parses the Beeline Timesheet to extract EmployeeData.
     *
     * @param beelineInputStream MultipartFile representing the Beeline Timesheet.
     * @return Map of EmployeeData from the Beeline Timesheet.
     */
    public static Map<String, Map<String, BeelineTimesheetEntry>> parseBeelineTimesheet(InputStream beelineInputStream) throws IOException {
        Map<String, Map<String, BeelineTimesheetEntry>> beelineEmployeeData = new HashMap<>();
        Workbook workbook = WorkbookFactory.create(beelineInputStream);
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) continue;

            String mcId = getCellValue(row.getCell(7)).toUpperCase(); // Column H for MC ID
            String timesheetDate = getCellValue(row.getCell(8));  // Column I for Timesheet Date
            String employeeName = getCellValue(row.getCell(1));  // Column B for Employee Name
            double totalUnits = parseDouble(getCellValue(row.getCell(6))); // Column G for Units

            BeelineTimesheetEntry beelineTimesheetEntry = new BeelineTimesheetEntry(mcId, employeeName, timesheetDate, totalUnits);
            Map<String, BeelineTimesheetEntry> timesheetData;
            if (beelineEmployeeData.containsKey(mcId)) {
                timesheetData = beelineEmployeeData.get(mcId);
            } else {
                timesheetData = new HashMap<>();
            }
            timesheetData.put(timesheetDate, beelineTimesheetEntry);
            beelineEmployeeData.put(mcId, timesheetData);
        }
        workbook.close();
        return beelineEmployeeData;
    }

    /**
     * Utility method to get the string value of a cell.
     */
    private static String getCellValue(Cell cell) {
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
    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static byte[] exportToExcel(List<Discrepancy> discrepancies) throws IOException {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filePath = "discrepancies_" + currentDate + ".xlsx";

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
    private static void deleteFile(File file) {
        if (file.exists()) {
            boolean isDeleted = file.delete();
            if (!isDeleted) {
                System.err.println("Failed to delete the file: " + file.getName());
            }
        }
    }

    public static boolean isRowEmpty(Row row) {
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

}
