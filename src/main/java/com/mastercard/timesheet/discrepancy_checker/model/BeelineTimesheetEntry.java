package com.mastercard.timesheet.discrepancy_checker.model;

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

    public BeelineTimesheetEntry() {

    }

    public String getMcId() {
        return mcId;
    }

    public void setMcId(String mcId) {
        this.mcId = mcId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getTimesheetDate() {
        return timesheetDate;
    }

    public void setTimesheetDate(String timesheetDate) {
        this.timesheetDate = timesheetDate;
    }

    public double getUnits() {
        return units;
    }

    public void setUnits(double units) {
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
