package com.mastercard.timesheet.discrepancy_checker.model;


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

    public PrismTimesheetEntry() {

    }

    public String getFdId() {
        return fdId;
    }

    public void setFdId(String fdId) {
        this.fdId = fdId;
    }

    public String getTimesheetDate() {
        return timesheetDate;
    }

    public void setTimesheetDate(String timesheetDate) {
        this.timesheetDate = timesheetDate;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getTypeOfHours() {
        return typeOfHours;
    }

    public void setTypeOfHours(String typeOfHours) {
        this.typeOfHours = typeOfHours;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
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
