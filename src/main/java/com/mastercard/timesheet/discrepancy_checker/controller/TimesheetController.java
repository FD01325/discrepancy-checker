package com.mastercard.timesheet.discrepancy_checker.controller;

import com.mastercard.timesheet.discrepancy_checker.model.Discrepancy;
import com.mastercard.timesheet.discrepancy_checker.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> compareTimesheets(
            @RequestParam("prismFile") MultipartFile prismFile,
            @RequestParam("beelineFile") MultipartFile beelineFile) {

        if (prismFile.isEmpty() || beelineFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "All two files (Prism and Beeline) are required."
            ));
        }

        try {
            List<Discrepancy> discrepancies = timesheetService.processTimesheets(prismFile, beelineFile);

            return ResponseEntity.ok(Map.of(
                    "message", discrepancies.isEmpty() ? "No discrepancies found." : "Discrepancy report generated.",
                    "discrepancyCount", discrepancies.size(),
                    "discrepancies", discrepancies
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "An error occurred while processing the timesheets.",
                    "error", e.getMessage()
            ));
        }
    }
}
