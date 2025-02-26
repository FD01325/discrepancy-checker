package com.mastercard.timesheet.discrepancy_checker.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeelineTimesheetEntry {
    private String mcId;
    private String employeeName;
    private String timesheetDate;
    private double units;

    public BeelineTimesheetEntry(String mcId, String employeeName, String timesheetDate, double units) {
        this.mcId = mcId;
        this.employeeName = employeeName;
        this.timesheetDate = timesheetDate;
        this.units = units;
    }

    @Override
    public String toString() {
        return "BeelineTimesheetEntry{" +
                "mcId='" + mcId + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", timesheetDate='" + timesheetDate + '\'' +
                ", units=" + units +
                '}';
    }
}
