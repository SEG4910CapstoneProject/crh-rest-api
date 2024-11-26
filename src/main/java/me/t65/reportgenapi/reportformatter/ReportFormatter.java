package me.t65.reportgenapi.reportformatter;

import org.springframework.http.ResponseEntity;

public interface ReportFormatter {
    ResponseEntity<?> format(RawReport rawReport);
}
