package com.mastercard.timesheet.discrepancy_checker.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeMappingEntry {
    private String fdEid;
    private String mcEid;
    private String resourceName;

    public EmployeeMappingEntry(String prismEmployeeId, String beelineEmployeeId, String employeeName) {
        this.fdEid = prismEmployeeId;
        this.mcEid = beelineEmployeeId;
        this.resourceName = employeeName;
    }

}
