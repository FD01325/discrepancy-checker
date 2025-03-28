package com.mastercard.timesheet.discrepancy_checker.service.impl;

import com.mastercard.timesheet.discrepancy_checker.model.BeelineTimesheetEntry;
import com.mastercard.timesheet.discrepancy_checker.model.Discrepancy;
import com.mastercard.timesheet.discrepancy_checker.model.PrismTimesheetEntry;
import com.mastercard.timesheet.discrepancy_checker.service.TimesheetService;
import com.mastercard.timesheet.discrepancy_checker.utils.ExcelParserUtils;
import com.mastercard.timesheet.discrepancy_checker.utils.TimesheetUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TimesheetServiceImpl implements TimesheetService {

    @Value("${file.beeline.path}")
    private String beelineFilePath;

    @Value("${file.prism.path}")
    private String prismFilePath;

    @Value("${file.report.path}")
    private String reportFilePath;

    private final Map<String, String> employeeMap = new HashMap<>();

    public TimesheetServiceImpl(@Value("${file.mapping.path}") String mappingFilePath) {
        loadEmployeeMappings(mappingFilePath);
    }

    /**
     * Loads employee mapping from EmployeeMapping.xlsx into a HashMap.
     */
    private void loadEmployeeMappings(String mappingFilePath) {
        try {
            employeeMap.putAll(ExcelParserUtils.loadEmployeeMapping(mappingFilePath));
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
        return processTimesheets(prismFile.getInputStream(), beelineFile.getInputStream());
    }

    private byte[] processTimesheets(InputStream prismInputStream, InputStream beelineInputStream) throws IOException {
        // Parse Prism and Beeline timesheets
        Map<String, MultiValuedMap<String, PrismTimesheetEntry>> prismTimesheets = ExcelParserUtils.parsePrismTimesheet(prismInputStream);
        Map<String, Map<String, BeelineTimesheetEntry>> beelineTimesheets = ExcelParserUtils.parseBeelineTimesheet(beelineInputStream);

        List<Discrepancy> discrepancies = new ArrayList<>();
        int srNo = 1;

        for (Map.Entry<String, Map<String, BeelineTimesheetEntry>> beelineEntry : beelineTimesheets.entrySet()) {
            String mcId = beelineEntry.getKey();
            Map<String, BeelineTimesheetEntry> beelineTimesheetData = beelineEntry.getValue();

            String fdId = TimesheetUtils.findKeyByValue(employeeMap, mcId);
            MultiValuedMap<String, PrismTimesheetEntry> prismTimesheetData = prismTimesheets.getOrDefault(fdId, new ArrayListValuedHashMap<>());
            identifyDiscrepancies(beelineTimesheetData, prismTimesheetData, discrepancies, srNo, fdId, mcId);
            prismTimesheets.remove(fdId);
            srNo++;
        }
        for (Map.Entry<String, MultiValuedMap<String, PrismTimesheetEntry>> prismEntry : prismTimesheets.entrySet()) {
            String fdId = prismEntry.getKey();
            MultiValuedMap<String, PrismTimesheetEntry> prismTimesheetData = prismEntry.getValue();

            String mcId = employeeMap.get(fdId);
            Map<String, BeelineTimesheetEntry> beelineTimesheetData = beelineTimesheets.getOrDefault(mcId, Map.of());
            identifyDiscrepancies(beelineTimesheetData, prismTimesheetData, discrepancies, srNo, fdId, mcId);
            srNo++;
        }
        return ExcelParserUtils.exportToExcel(discrepancies);
    }

    @Override
    public byte[] processTimesheets() throws FileNotFoundException {
        File beelineFolder = new File(beelineFilePath);
        File[] beelineFiles = beelineFolder.listFiles();

        File prismFolder = new File(prismFilePath);
        File[] prismFiles = prismFolder.listFiles();

        if (beelineFiles != null && prismFiles != null && beelineFiles.length == 1 && prismFiles.length == 1) {
            try (InputStream beelineInputStream = new FileInputStream(beelineFiles[0]);
                 InputStream prismInputStream = new FileInputStream(prismFiles[0])) {
                byte[] bytes = processTimesheets(prismInputStream, beelineInputStream);
                String currentTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                String fileName = "discrepancy_report_" + currentTime + ".xlsx";

                // Create the folder if it doesn't exist
                File folder = new File(reportFilePath);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                File file = new File(folder, fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(bytes);
                    System.out.println("File created at: " + file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                System.out.println("Exception");
            }
        }
        return null;
    }

    private void identifyDiscrepancies(Map<String, BeelineTimesheetEntry> beelineTimesheetData, MultiValuedMap<String, PrismTimesheetEntry> prismTimesheetData, List<Discrepancy> discrepancies, int srNo, String fdId, String mcId) {
        List<String> sortedDates = TimesheetUtils.getSortedDates(beelineTimesheetData.keySet(), prismTimesheetData.keySet());

        for (String date : sortedDates) {
            BeelineTimesheetEntry beelineTimesheetEntry = beelineTimesheetData.get(date);
            ArrayList<PrismTimesheetEntry> prismTimesheetEntries = new ArrayList<>(prismTimesheetData.get(date));
            String prismHours, beelineHours, type, reason, prismHours2, type2;

            if (beelineTimesheetEntry != null) {
                beelineHours = String.valueOf(beelineTimesheetEntry.getUnits());

                if (prismTimesheetEntries.isEmpty()) {
                    reason = "Timesheet mismatch in Prism and Beeline timesheets. Prism timesheet not filled, Beeline Units: " + beelineHours;
                    Discrepancy discrepancy = Discrepancy.builder().srNo(srNo).resourceName(beelineTimesheetEntry.getEmployeeName()).fdId(fdId).mcId(mcId).timesheetDate(date).discrepancyReason(reason).build();
                    discrepancies.add(discrepancy);
                } else if (prismTimesheetEntries.size() == 1) {
                    prismHours = String.valueOf(prismTimesheetEntries.get(0).getTotalHours());
                    type = prismTimesheetEntries.get(0).getTypeOfHours();

                    if ((isWorkingType(type) && "8.0".equals(prismHours) && ("1.0".equals(beelineHours) || "8.0".equals(beelineHours))) || (!isWorkingType(type) && "8.0".equals(prismHours) && "0.0".equals(beelineHours))) {
                        reason = "There is no discrepancy";
                    } else {
                        reason = "Timesheet mismatch in Prism and Beeline timesheets. Type of Hours: " + type + ", Total Hours in Prism: " + prismHours + ", Beeline Units: " + beelineHours;
                    }
                    Discrepancy discrepancy = Discrepancy.builder().srNo(srNo).resourceName(prismTimesheetEntries.get(0).getEmployeeName()).fdId(fdId).mcId(mcId).timesheetDate(date).discrepancyReason(reason).build();
                    discrepancies.add(discrepancy);
                } else if (prismTimesheetEntries.size() == 2) { // half day
                    prismHours = String.valueOf(prismTimesheetEntries.get(0).getTotalHours());
                    type = prismTimesheetEntries.get(0).getTypeOfHours();
                    prismHours2 = String.valueOf(prismTimesheetEntries.get(1).getTotalHours());
                    type2 = prismTimesheetEntries.get(1).getTypeOfHours();

                    if (("Leave".equalsIgnoreCase(type) && isWorkingType(type2) && "4.0".equals(prismHours2) && ("4.0".equals(beelineHours) || "0.5".equals(beelineHours))) || ("Leave".equalsIgnoreCase(type2) && isWorkingType(type) && "4.0".equals(prismHours) && ("4.0".equals(beelineHours) || "0.5".equals(beelineHours)))) {
                        reason = "There is no discrepancy";
                    } else {
                        reason = "Timesheet mismatch in Prism and Beeline timesheets. Type of Hours: " + type + ", Total Hours in Prism: " + prismHours + ", Type of Hours: " + type2 + ", Total Hours in Prism: " + prismHours2 + ", Beeline Units: " + beelineHours;
                    }
                    Discrepancy discrepancy = Discrepancy.builder().srNo(srNo).resourceName(prismTimesheetEntries.get(0).getEmployeeName()).fdId(fdId).mcId(mcId).timesheetDate(date).discrepancyReason(reason).build();
                    discrepancies.add(discrepancy);
                }
            } else {
                prismHours = String.valueOf(prismTimesheetEntries.get(0).getTotalHours());
                type = prismTimesheetEntries.get(0).getTypeOfHours();
                reason = "Timesheet mismatch in Prism and Beeline timesheets. Type of Hours: " + type + ", Total Hours in Prism: " + prismHours + ", Beeline timesheet not filled";
                Discrepancy discrepancy = Discrepancy.builder().srNo(srNo).resourceName(prismTimesheetEntries.get(0).getEmployeeName()).fdId(fdId).mcId(mcId).timesheetDate(date).discrepancyReason(reason).build();
                discrepancies.add(discrepancy);
            }
        }
    }

    private boolean isWorkingType(String type) {
        return !"Leave".equalsIgnoreCase(type) && !"Public Holiday".equalsIgnoreCase(type);
    }
}
