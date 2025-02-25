package com.mastercard.timesheet.discrepancy_checker.controller;

import com.mastercard.timesheet.discrepancy_checker.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/timesheet")
public class TimesheetController {

    @Autowired
    private TimesheetService timesheetService;

    /**
     * Endpoint to upload Prism Timesheet and Beeline Timesheet files
     * and generate the discrepancy report.
     */
    @PostMapping("/compare")
    public ResponseEntity<byte[]> compareTimesheets(
            @RequestParam("prismFile") MultipartFile prismFile,
            @RequestParam("beelineFile") MultipartFile beelineFile) throws Exception {
        byte[] fileContent = timesheetService.processTimesheets(prismFile, beelineFile);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=discrepancies.xlsx");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // Return the Excel file as a response
        return ResponseEntity.ok().headers(headers).body(fileContent);
    }
}
