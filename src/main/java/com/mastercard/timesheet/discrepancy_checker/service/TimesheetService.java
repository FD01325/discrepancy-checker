package com.mastercard.timesheet.discrepancy_checker.service;

import com.mastercard.timesheet.discrepancy_checker.model.BeelineTimesheetEntry;
import com.mastercard.timesheet.discrepancy_checker.model.Discrepancy;
import com.mastercard.timesheet.discrepancy_checker.model.PrismTimesheetEntry;
import com.mastercard.timesheet.discrepancy_checker.parser.ExcelParser;
import org.apache.commons.collections4.MultiValuedMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TimesheetService {

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
            employeeMap.putAll(ExcelParser.loadEmployeeMapping());
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
    public byte[] processTimesheets(MultipartFile prismFile, MultipartFile beelineFile) throws Exception {
        // Parse Prism and Beeline timesheets
        Map<String, MultiValuedMap<String, PrismTimesheetEntry>> prismTimesheets = excelParser.parsePrismTimesheet(prismFile);
        Map<String, Map<String, BeelineTimesheetEntry>> beelineTimesheets = excelParser.parseBeelineTimesheet(beelineFile);

        List<Discrepancy> discrepancies = new ArrayList<>();
        int srNo = 1;

        //<mcid, <date, beelinetimesheet>>
        //<fdid, <date, prismtimesheet>>
        //<fcId, mcId>

        for (Map.Entry<String, Map<String, BeelineTimesheetEntry>> beelineEntry : beelineTimesheets.entrySet()) {
            String mcId = beelineEntry.getKey();
            Map<String, BeelineTimesheetEntry> beelineTimesheetData = beelineEntry.getValue();

            String fcId = findKeyByValue(employeeMap, mcId);
            MultiValuedMap<String, PrismTimesheetEntry> prismTimesheetData = prismTimesheets.get(fcId);

            List<String> sortedDates = getSortedDates(beelineTimesheetData.keySet(), prismTimesheetData.keySet());

            for (String date : sortedDates) {
                BeelineTimesheetEntry beelineTimesheetEntry = beelineTimesheetData.get(date);
                ArrayList<PrismTimesheetEntry> prismTimesheetEntries = new ArrayList<>(prismTimesheetData.get(date));
                String prismHours = null, beelineHours = null, type = null, reason = null, prismHours2 = null, type2 = null;

                if (beelineTimesheetEntry != null && !prismTimesheetEntries.isEmpty()) {
                    beelineHours = String.valueOf(beelineTimesheetEntry.getUnits());

                    // full day
                    if (prismTimesheetEntries.size() == 1) {
                        prismHours = String.valueOf(prismTimesheetEntries.get(0).getTotalHours());
                        type = prismTimesheetEntries.get(0).getTypeOfHours();

                        if ((isWorkingType(type) && "8".equals(prismHours) && ("1".equals(beelineHours) || "8".equals(beelineHours))) ||
                                (!isWorkingType(type) && "8".equals(prismHours) && "0".equals(beelineHours))) {

                        } else {
                            reason = "Timesheet mismatch in Prism and Beeline timesheets";
                        }
                    } else if (prismTimesheetEntries.size() == 2) {
                        prismHours = String.valueOf(prismTimesheetEntries.get(0).getTotalHours());
                        type = prismTimesheetEntries.get(0).getTypeOfHours();
                        prismHours2 = String.valueOf(prismTimesheetEntries.get(1).getTotalHours());
                        type2 = prismTimesheetEntries.get(1).getTypeOfHours();

                        if (("Leave".equalsIgnoreCase(type) && isWorkingType(type2) && "4".equals(prismHours) && ("4".equals(beelineHours) || "0.5".equals(beelineHours))) ||
                                ("Leave".equalsIgnoreCase(type2) && isWorkingType(type) && "4".equals(prismHours) && ("4".equals(beelineHours) || "0.5".equals(beelineHours)))) {

                        } else {
                            reason = "Timesheet mismatch in Prism and Beeline timesheets";
                        }
                    }
                }

                Discrepancy discrepancy = Discrepancy.builder().srNo(srNo).resourceName(prismTimesheetEntries.get(0).getEmployeeName()).fdId(fcId).mcId(mcId).timesheetDate(date).discrepancyReason(reason).build();
                discrepancies.add(discrepancy);
            }
            srNo++;
        }
        byte[] bytes = excelParser.exportToExcel(discrepancies);
        return bytes;
    }

    private boolean isWorkingType(String type) {
        if ("Leave".equalsIgnoreCase(type) || "Public Holiday".equalsIgnoreCase(type)) {
            return false;
        }
        return true;
    }

    public static String findKeyByValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return "";  // Return an empty string if the value is not found
    }

    // Utility method to merge and sort the dates from both maps
    public static List<String> getSortedDates(Set<String> set1, Set<String> set2) {
        Set<String> allDates = new HashSet<>();

        // Add all the dates from both maps
        allDates.addAll(set1);
        allDates.addAll(set2);

        // Define the date formatter to parse the date strings
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

        // Convert the set of dates into a list of LocalDate objects and sort
        List<String> sortedDates = new ArrayList<>(allDates);
        sortedDates.sort((date1, date2) -> {
            LocalDate localDate1 = LocalDate.parse(date1, formatter);
            LocalDate localDate2 = LocalDate.parse(date2, formatter);
            return localDate1.compareTo(localDate2); // Ascending order
        });

        return sortedDates;
    }
}
