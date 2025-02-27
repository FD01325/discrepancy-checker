package com.mastercard.timesheet.discrepancy_checker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class BeelineTimesheetEntry {
    private String mcId;
    private String employeeName;
    private String timesheetDate;
    private double units;
}
