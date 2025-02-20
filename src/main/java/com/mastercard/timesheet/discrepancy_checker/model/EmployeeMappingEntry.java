package com.mastercard.timesheet.discrepancy_checker.model;

public class EmployeeMappingEntry {
    private String fdEid;
    private String mcEid;
    private String resourceName;

    public EmployeeMappingEntry(String prismEmployeeId, String beelineEmployeeId, String employeeName) {
        this.fdEid = prismEmployeeId;
        this.mcEid = beelineEmployeeId;
        this.resourceName = employeeName;
    }

    public String getFdEid() {
        return fdEid;
    }
    public void setFdEid(String fdEid) {
        this.fdEid = fdEid;
    }
    public String getMcEid() {
        return mcEid;
    }
    public void setMcEid(String mcEid) {
        this.mcEid = mcEid;
    }
    public String getResourceName() {
        return resourceName;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
}
