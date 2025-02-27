package com.mastercard.timesheet.discrepancy_checker.service.impl;

import com.mastercard.timesheet.discrepancy_checker.model.BeelineTimesheetEntry;
import com.mastercard.timesheet.discrepancy_checker.model.Discrepancy;
import com.mastercard.timesheet.discrepancy_checker.model.PrismTimesheetEntry;
import com.mastercard.timesheet.discrepancy_checker.service.TimesheetService;
import com.mastercard.timesheet.discrepancy_checker.utils.ExcelParserUtils;
import com.mastercard.timesheet.discrepancy_checker.utils.TimesheetUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimesheetServiceImpl implements TimesheetService {

    private final Map<String, String> employeeMap = new HashMap<>();

    public TimesheetServiceImpl() {
        loadEmployeeMappings();
    }

    /**
     * Loads employee mapping from EmployeeMapping.xlsx into a HashMap.
     */
    private void loadEmployeeMappings() {
        try {
            employeeMap.putAll(ExcelParserUtils.loadEmployeeMapping());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Employee Mapping: " + e.getMessage());
        }
    }

    /**
     * Process Prism and Beeline timesheets, compare them, and generate discrepancies.
     * Map of MCID and beeline timesheet data - <mcid, <date, beelinetimesheet>>
     * Map of FDID and prism timesheet data - <fdid, <date, prismtimesheet>>
     * Map of Employee Id mappings - <fdId, mcId>
     */
    @Override
    public byte[] processTimesheets(MultipartFile prismFile, MultipartFile beelineFile) throws Exception {
        // Parse Prism and Beeline timesheets
        Map<String, MultiValuedMap<String, PrismTimesheetEntry>> prismTimesheets = ExcelParserUtils.parsePrismTimesheet(prismFile);
        Map<String, Map<String, BeelineTimesheetEntry>> beelineTimesheets = ExcelParserUtils.parseBeelineTimesheet(beelineFile);

        List<Discrepancy> discrepancies = new ArrayList<>();
        int srNo = 1;

        for (Map.Entry<String, Map<String, BeelineTimesheetEntry>> beelineEntry : beelineTimesheets.entrySet()) {
            String mcId = beelineEntry.getKey();
            Map<String, BeelineTimesheetEntry> beelineTimesheetData = beelineEntry.getValue();

            String fdId = TimesheetUtils.findKeyByValue(employeeMap, mcId);
            MultiValuedMap<String, PrismTimesheetEntry> prismTimesheetData = prismTimesheets.get(fdId);

            if (prismTimesheetData == null) {
                System.out.println("FD ID: " + fdId + " MC ID: " + mcId);
                continue;
            }
            List<String> sortedDates = TimesheetUtils.getSortedDates(beelineTimesheetData.keySet(), prismTimesheetData.keySet());

            for (String date : sortedDates) {
                BeelineTimesheetEntry beelineTimesheetEntry = beelineTimesheetData.get(date);
                ArrayList<PrismTimesheetEntry> prismTimesheetEntries = new ArrayList<>(prismTimesheetData.get(date));
                String prismHours, beelineHours, type, reason, prismHours2, type2;

                if (beelineTimesheetEntry != null && !prismTimesheetEntries.isEmpty()) {
                    beelineHours = String.valueOf(beelineTimesheetEntry.getUnits());

                    // full day
                    if (prismTimesheetEntries.size() == 1) {
                        prismHours = String.valueOf(prismTimesheetEntries.get(0).getTotalHours());
                        type = prismTimesheetEntries.get(0).getTypeOfHours();

                        if ((isWorkingType(type) && "8.0".equals(prismHours) && ("1.0".equals(beelineHours) || "8.0".equals(beelineHours))) ||
                                (!isWorkingType(type) && "8.0".equals(prismHours) && "0.0".equals(beelineHours))) {
                            reason = "There is no discrepancy";
                        } else {
                            reason = "Timesheet mismatch in Prism and Beeline timesheets";
                        }
                        Discrepancy discrepancy = Discrepancy.builder().srNo(srNo).resourceName(prismTimesheetEntries.get(0).getEmployeeName()).fdId(fdId).mcId(mcId).timesheetDate(date).discrepancyReason(reason).build();
                        discrepancies.add(discrepancy);
                    } else if (prismTimesheetEntries.size() == 2) { // half day
                        prismHours = String.valueOf(prismTimesheetEntries.get(0).getTotalHours());
                        type = prismTimesheetEntries.get(0).getTypeOfHours();
                        prismHours2 = String.valueOf(prismTimesheetEntries.get(1).getTotalHours());
                        type2 = prismTimesheetEntries.get(1).getTypeOfHours();

                        if (("Leave".equalsIgnoreCase(type) && isWorkingType(type2) && "4.0".equals(prismHours2) && ("4.0".equals(beelineHours) || "0.5".equals(beelineHours))) ||
                                ("Leave".equalsIgnoreCase(type2) && isWorkingType(type) && "4.0".equals(prismHours) && ("4.0".equals(beelineHours) || "0.5".equals(beelineHours)))) {
                            reason = "There is no discrepancy";
                        } else {
                            reason = "Timesheet mismatch in Prism and Beeline timesheets";
                        }
                        Discrepancy discrepancy = Discrepancy.builder().srNo(srNo).resourceName(prismTimesheetEntries.get(0).getEmployeeName()).fdId(fdId).mcId(mcId).timesheetDate(date).discrepancyReason(reason).build();
                        discrepancies.add(discrepancy);
                    }
                }


            }
            srNo++;
        }
        return ExcelParserUtils.exportToExcel(discrepancies);
    }

    private boolean isWorkingType(String type) {
        return !"Leave".equalsIgnoreCase(type) && !"Public Holiday".equalsIgnoreCase(type);
    }
}
