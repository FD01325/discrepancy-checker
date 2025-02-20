package com.mastercard.timesheet.discrepancy_checker.service;

import com.mastercard.timesheet.discrepancy_checker.model.BeelineTimesheetEntry;
import com.mastercard.timesheet.discrepancy_checker.model.Discrepancy;
import com.mastercard.timesheet.discrepancy_checker.model.PrismTimesheetEntry;
import com.mastercard.timesheet.discrepancy_checker.parser.ExcelParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimesheetService {

    private static final String EMPLOYEE_MAPPING_FILE_PATH = "D:\\git\\discrepancy-checker\\backend\\src\\main\\resources\\EmployeeMapping.xlsx";

    private final Map<String, String> employeeMap = new HashMap<>();

    private ExcelParser excelParser;

    public TimesheetService(ExcelParser excelParser) {
        this.excelParser = excelParser;
    }


    public TimesheetService() {
        loadEmployeeMappings();
    }

    /**
     * Loads employee mapping from EmployeeMapping.xlsx into a HashMap.
     */
    private void loadEmployeeMappings() {
        try {
            employeeMap.putAll(ExcelParser.loadEmployeeMapping(new File(EMPLOYEE_MAPPING_FILE_PATH)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Employee Mapping: " + e.getMessage());
        }
    }

    /**
     * Find MC ID using FD ID.
     *
     * @param fdId FD Employee ID (from Prism Timesheet)
     * @return Corresponding MC ID (from Beeline Timesheet)
     */
    private String findMCIdByFdId(String fdId) {
        return employeeMap.getOrDefault(fdId, null);
    }

    /**
     * Process Prism and Beeline timesheets, compare them, and generate discrepancies.
     */
    public List<Discrepancy> processTimesheets(MultipartFile prismFile, MultipartFile beelineFile) throws Exception {
        // Parse Prism and Beeline timesheets
        List<PrismTimesheetEntry> prismTimesheets = excelParser.parsePrismTimesheet(prismFile);
        List<BeelineTimesheetEntry> beelineTimesheets = excelParser.parseBeelineTimesheet(beelineFile);

        // Load Employee Mapping (FD EID ↔ MC EID)
        Map<String, String> employeeMap = ExcelParser.loadEmployeeMapping(new File(EMPLOYEE_MAPPING_FILE_PATH));

        List<Discrepancy> discrepancies = new ArrayList<>();
        int srNo = 1;

        // Compare timesheets based on mapped Employee IDs
        for (PrismTimesheetEntry prism : prismTimesheets) {
            String fdId = prism.getFdId();
            String resourceName = null;
            String mcId = null;

            // Find corresponding MC ID from Employee Mapping
            for (Map.Entry<String, String> entry : employeeMap.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(fdId)) {
                    resourceName = entry.getKey();
                    mcId = entry.getValue();
                    break;
                }
            }

            if (mcId == null) {
                discrepancies.add(new Discrepancy(srNo++, resourceName, fdId, "N/A",
                        prism.getTimesheetDate(), "No matching MC ID in Beeline"));
                continue;
            }

            // Find matching record in Beeline timesheet
            String finalMcId = mcId;
            BeelineTimesheetEntry beeline = beelineTimesheets.stream()
                    .filter(b -> b.getMcId().equalsIgnoreCase(finalMcId))
                    .findFirst()
                    .orElse(null);

            if (beeline == null) {
                discrepancies.add(new Discrepancy(srNo++, resourceName, fdId, mcId,
                        prism.getTimesheetDate(), "Employee not found in Beeline Timesheet"));
                continue;
            }

            // Compare hours worked
            if (prism.getTotalHours() != beeline.getUnits()) {
                discrepancies.add(new Discrepancy(srNo++, resourceName, fdId, mcId,
                        prism.getTimesheetDate(), "Mismatch in worked hours: Prism = " +
                        prism.getTotalHours() + ", Beeline = " + beeline.getUnits()));
            }
        }

        return discrepancies;
    }
}
