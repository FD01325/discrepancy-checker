package com.mastercard.timesheet.discrepancy_checker.parser;

import com.mastercard.timesheet.discrepancy_checker.model.EmployeeMappingEntry;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.*;

public class EmployeeMappingParser {

    public static Map<String, EmployeeMappingEntry> parseEmployeeMapping(InputStream inputStream) throws Exception {
        Map<String, EmployeeMappingEntry> mapping = new HashMap<>();

        Workbook workbook = new XSSFWorkbook(inputStream);

        // Parse FFS List
        Sheet ffsSheet = workbook.getSheet("FFS List");
        for (int i = 1; i <= ffsSheet.getLastRowNum(); i++) {  // Skip header row
            Row row = ffsSheet.getRow(i);
            if (row != null) {
                String resourceName = row.getCell(3).getStringCellValue().trim();  // Column B
                String fdEid = row.getCell(5).getStringCellValue().trim();  // Column F
                mapping.putIfAbsent(resourceName, new EmployeeMappingEntry(resourceName, fdEid, null));
            }
        }

        // Parse MC List
        Sheet mcSheet = workbook.getSheet("MC List");
        for (int i = 1; i <= mcSheet.getLastRowNum(); i++) {  // Skip header row
            Row row = mcSheet.getRow(i);
            if (row != null) {
                String resourceName = row.getCell(1).getStringCellValue().trim();  // Column B
                String mcEid = row.getCell(2).getStringCellValue().trim();  // Column C
                mapping.computeIfPresent(resourceName, (key, entry) -> {
                    entry.setMcEid(mcEid);
                    return entry;
                });
            }
        }

        workbook.close();
        return mapping;
    }
}
