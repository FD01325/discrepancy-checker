package com.mastercard.timesheet.discrepancy_checker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Discrepancy {
    private int srNo;
    private String resourceName;
    private String fdId;
    private String mcId;
    private String timesheetDate;
    private String discrepancyReason;
}
