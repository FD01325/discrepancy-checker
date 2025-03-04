package com.mastercard.timesheet.discrepancy_checker.controller;

import com.mastercard.timesheet.discrepancy_checker.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/timesheet")
public class TimesheetController {

    @Autowired
    private TimesheetService timesheetService;

    /**
     * Endpoint to upload Prism and Beeline Timesheet files from user, compare them,
     * and generate a discrepancy report in the form of an Excel file.
     */
    @PostMapping("/compare")
    public ResponseEntity<byte[]> compareTimesheets(
            @RequestParam("prismFile") MultipartFile prismFile,
            @RequestParam("beelineFile") MultipartFile beelineFile) throws Exception {

        // Generate filename with current date
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "discrepancies_" + currentDate + ".xlsx";

        // Process the timesheets
        byte[] fileContent = timesheetService.processTimesheets(prismFile, beelineFile);

        // Set response headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // Return the Excel file as a response
        return ResponseEntity.ok().headers(headers).body(fileContent);
    }

    /**
     * Endpoint to upload Prism and Beeline Timesheet files, compare them,
     * and generate a discrepancy report in the form of an Excel file.
     */
    @PostMapping("/compare/new")
    public ResponseEntity<byte[]> compareTimesheets() throws Exception {
        byte[] fileContent = timesheetService.processTimesheets();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=discrepancies.xlsx");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // Return the Excel file as a response
        return ResponseEntity.ok().headers(headers).body(fileContent);
    }
}
