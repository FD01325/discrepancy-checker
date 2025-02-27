package com.mastercard.timesheet.discrepancy_checker.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class PrismTimesheetEntry {
    private String fdId;
    private String timesheetDate;
    private String employeeName;
    private String typeOfHours;
    private double totalHours;
}
