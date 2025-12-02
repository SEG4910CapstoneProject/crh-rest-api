package me.t65.reportgenapi.db.services;

import org.springframework.http.ResponseEntity;

public interface EmailService {
    boolean sendReportByEmail(
            ResponseEntity<?> formattedReport, String[] recipientEmails, String title);
}
