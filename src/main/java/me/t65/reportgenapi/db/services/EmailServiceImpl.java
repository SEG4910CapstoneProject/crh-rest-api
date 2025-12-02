package me.t65.reportgenapi.db.services;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImpl implements EmailService {

    public boolean sendReportByEmail(
            ResponseEntity<?> formattedReport, String[] recipientEmails, String title) {
        String htmlReport = (String) formattedReport.getBody();
  
        for (String email : recipientEmails) {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", htmlReport.getBytes(StandardCharsets.UTF_8))
                .filename("report.html")
                .contentType(MediaType.TEXT_HTML);
            builder.part("subject", title);
            builder.part("to", email);
            WebClient.create("http://crh-email-service:5000/send-email")
                    .post()
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        }
        return true;
    }
}
