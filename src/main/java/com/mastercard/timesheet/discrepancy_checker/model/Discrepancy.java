package com.mastercard.timesheet.discrepancy_checker.model;

import java.util.Objects;

public class Discrepancy {

    private int srNo;
    private String resourceName;
    private String fdId;
    private String mcId;
    private String timesheetDate;
    private String discrepancyReason;

    public Discrepancy(int srNo, String resourceName, String fdId, String mcId, String timesheetDate, String discrepancyReason) {
        this.srNo = srNo;
        this.resourceName = resourceName;
        this.fdId = fdId;
        this.mcId = mcId;
        this.timesheetDate = timesheetDate;
        this.discrepancyReason = discrepancyReason;
    }

    public int getSrNo() {
        return srNo;
    }

    public void setSrNo(int srNo) {
        this.srNo = srNo;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getFdId() {
        return fdId;
    }

    public void setFdId(String fdId) {
        this.fdId = fdId;
    }

    public String getMcId() {
        return mcId;
    }

    public void setMcId(String mcId) {
        this.mcId = mcId;
    }

    public String getTimesheetDate() {
        return timesheetDate;
    }

    public void setTimesheetDate(String timesheetDate) {
        this.timesheetDate = timesheetDate;
    }

    public String getDiscrepancyReason() {
        return discrepancyReason;
    }

    public void setDiscrepancyReason(String discrepancyReason) {
        this.discrepancyReason = discrepancyReason;
    }

    @Override
    public String toString() {
        return "Discrepancy{" +
                "srNo=" + srNo +
                ", resourceName='" + resourceName + '\'' +
                ", fdId='" + fdId + '\'' +
                ", mcId='" + mcId + '\'' +
                ", timesheetDate='" + timesheetDate + '\'' +
                ", discrepancyReason='" + discrepancyReason + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Discrepancy that = (Discrepancy) o;
        return srNo == that.srNo && Objects.equals(resourceName, that.resourceName) && Objects.equals(fdId, that.fdId) && Objects.equals(mcId, that.mcId) && Objects.equals(timesheetDate, that.timesheetDate) && Objects.equals(discrepancyReason, that.discrepancyReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(srNo, resourceName, fdId, mcId, timesheetDate, discrepancyReason);
    }
}
