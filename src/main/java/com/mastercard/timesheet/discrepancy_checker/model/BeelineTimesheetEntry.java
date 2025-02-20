package com.mastercard.timesheet.discrepancy_checker.model;

public class BeelineTimesheetEntry {
    private String mcId;
    private String employeeName;
    private double units;

    public BeelineTimesheetEntry(String mcId, String employeeName, double units) {
        this.mcId = mcId;
        this.employeeName = employeeName;
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

    public double getUnits() {
        return units;
    }

    public void setUnits(double units) {
        this.units = units;
    }

    @Override
    public String toString() {
        return "BeelineTimesheetEntry{" +
                "employeeId='" + mcId + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", units=" + units +
                '}';
    }
}
