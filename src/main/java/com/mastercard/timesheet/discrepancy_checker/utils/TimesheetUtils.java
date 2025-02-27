package com.mastercard.timesheet.discrepancy_checker.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimesheetUtils {

    // Utility method to merge and sort the dates from both maps
    public static List<String> getSortedDates(Set<String> set1, Set<String> set2) {
        Set<String> allDates = new HashSet<>();

        // Add all the dates from both maps
        allDates.addAll(set1);
        allDates.addAll(set2);

        // Define the date formatter to parse the date strings
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

        // Convert the set of dates into a list of LocalDate objects and sort
        List<String> sortedDates = new ArrayList<>(allDates);
        sortedDates.sort((date1, date2) -> {
            LocalDate localDate1 = LocalDate.parse(date1, formatter);
            LocalDate localDate2 = LocalDate.parse(date2, formatter);
            return localDate1.compareTo(localDate2); // Ascending order
        });

        return sortedDates;
    }

    public static String findKeyByValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return "";  // Return an empty string if the value is not found
    }
}
