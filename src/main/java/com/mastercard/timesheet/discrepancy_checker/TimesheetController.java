package com.mastercard.timesheet.discrepancy_checker;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api/timesheets")
public class TimesheetController {

    private static final Logger logger = LoggerFactory.getLogger(TimesheetController.class);

    @PostMapping("/compare")
    public List<Map<String, String>> compareTimesheets(
            @RequestParam("prismFile") MultipartFile prismFile,
            @RequestParam("beelineFile") MultipartFile beelineFile) {

        try {
            List<Map<String, String>> prismData = parseExcel(prismFile);
            List<Map<String, String>> beelineData = parseExcel(beelineFile);
            return findDiscrepancies(prismData, beelineData);
        } catch (Exception e) {
            logger.error("Failed to process files", e);
            return Collections.singletonList(Map.of("Error", "Failed to process files: " + e.getMessage()));
        }
    }

    private List<Map<String, String>> parseExcel(MultipartFile file) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();

            // Extract headers
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim());
            }

            // Extract rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                Map<String, String> record = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    record.put(headers.get(j), getCellValue(cell));
                }
                records.add(record);
            }
        }
        return records;
    }

    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return cell.toString().trim();
        }
    }

    private List<Map<String, String>> findDiscrepancies(List<Map<String, String>> prismData, List<Map<String, String>> beelineData) {
        List<Map<String, String>> discrepancies = new ArrayList<>();

        for (Map<String, String> prismRecord : prismData) {
            String prismEmployeeId = prismRecord.get("User Employee ID");
            String prismDate = prismRecord.get("Timesheet Date");
            String prismHours = prismRecord.get("Total Hours");

            for (Map<String, String> beelineRecord : beelineData) {
                String beelineEmployeeId = beelineRecord.get("ID");
                String beelineDate = beelineRecord.get("Timesheet date");
                String beelineUnits = beelineRecord.get("Units");

                if (prismEmployeeId.equals(beelineEmployeeId) && prismDate.equals(beelineDate)) {
                    if (!prismHours.equals(beelineUnits)) {
                        Map<String, String> discrepancy = new HashMap<>();
                        discrepancy.put("Employee ID", prismEmployeeId);
                        discrepancy.put("Date", prismDate);
                        discrepancy.put("Prism Hours", prismHours);
                        discrepancy.put("Beeline Units", beelineUnits);
                        discrepancy.put("Error", "Mismatch between Prism and Beeline");
                        discrepancies.add(discrepancy);
                    }
                }
            }
        }

        return discrepancies;
    }
}
