package me.t65.reportgenapi.reportformatter;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import me.t65.reportgenapi.generators.JsonReportGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class JsonReportFormatter implements ReportFormatter {

    @Autowired private JsonReportGenerator jsonReportGenerator;

    @Override
    public ResponseEntity<?> format(RawReport rawReport) {
        return ResponseEntity.ok(jsonReportGenerator.generateFromRawReport(rawReport));
    }
}
