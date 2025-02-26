package com.mastercard.timesheet.discrepancy_checker.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrismTimesheetEntry {
    private String fdId;
    private String timesheetDate;
    private String employeeName;
    private String typeOfHours;
    private double totalHours;

    public PrismTimesheetEntry(String fdId, String timesheetDate, String employeeName, String typeOfHours, double totalHours) {
        this.fdId = fdId;
        this.timesheetDate = timesheetDate;
        this.employeeName = employeeName;
        this.typeOfHours = typeOfHours;
        this.totalHours = totalHours;
    }

    @Override
    public String toString() {
        return "PrismTimesheetEntry{" +
                "fdId='" + fdId + '\'' +
                ", timesheetDate='" + timesheetDate + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", typeOfHours='" + typeOfHours + '\'' +
                ", totalHours=" + totalHours +
                '}';
    }
}
