package com.mastercard.timesheet.discrepancy_checker.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;

public interface TimesheetService {

    byte[] processTimesheets(MultipartFile prismFile, MultipartFile beelineFile) throws Exception;

    byte[] processTimesheets() throws FileNotFoundException;
}
