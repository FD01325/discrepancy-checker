package com.mastercard.timesheet.discrepancy_checker.service;

import org.springframework.web.multipart.MultipartFile;

public interface TimesheetService {

    byte[] processTimesheets(MultipartFile prismFile, MultipartFile beelineFile) throws Exception;

}
