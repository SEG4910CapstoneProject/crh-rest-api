package me.t65.reportgenapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.db.services.DbArticlesService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatbotApiController {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final DbArticlesService dbArticlesService;

    public ChatbotApiController(DbArticlesService dbArticlesService) {
        this.dbArticlesService = dbArticlesService;
    }

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody Map<String, String> requestBody) {
        try {
            String message = requestBody.get("message");
            String articleContent = null;

            String uuidRegex =
                    "\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b";
            var uuidMatcher = Pattern.compile(uuidRegex).matcher(message);
            if (uuidMatcher.find()) {
                UUID articleId = UUID.fromString(uuidMatcher.group());
                Optional<JsonArticleReportResponse> article =
                        dbArticlesService.getArticleById(articleId);
                if (article.isPresent()) {
                    articleContent = article.get().getDescription();
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Article with ID " + articleId + " not found.");
                }
            } else {
                String urlRegex = "(https?://\\S+)";
                var urlMatcher = Pattern.compile(urlRegex).matcher(message);
                if (urlMatcher.find()) {
                    String url = urlMatcher.group();
                    Optional<JsonArticleReportResponse> article =
                            dbArticlesService.getArticleByLink(url);
                    if (article.isPresent()) {
                        articleContent = article.get().getDescription();
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Article with URL " + url + " not found.");
                    }
                }
            }

            String prompt =
                    (articleContent != null)
                            ? "Summarize this article:\n" + articleContent
                            : message;

            String json =
                    mapper.writeValueAsString(Map.of("model", "llama3.2:3b", "prompt", prompt));
            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create("http://ollama:11434/v1/completions"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

            HttpResponse<String> response =
                    client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> map = mapper.readValue(response.body(), Map.class);
            var choices = (java.util.List<Map<String, Object>>) map.get("choices");
            String summary = choices.get(0).get("text").toString();
            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
