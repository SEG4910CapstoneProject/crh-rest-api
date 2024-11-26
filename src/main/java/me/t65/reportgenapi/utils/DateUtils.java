package me.t65.reportgenapi.utils;

import me.t65.reportgenapi.db.postgres.entities.ReportType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

public class DateUtils {

    public static boolean isValidDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidSearch(String start, String end) {
        try {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            return startDate.isBefore(endDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidType(String type) {
        for (ReportType reportType : ReportType.values()) {
            if (reportType.name().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    public static LocalDateTime getLastModifiedFromInstant(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
