package me.t65.reportgenapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.services.DbArticlesService;
import me.t65.reportgenapi.llm.OllamaClient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatbotApiController {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final DbArticlesService dbArticlesService;
    private final OllamaClient ollamaClient;

    public ChatbotApiController(DbArticlesService dbArticlesService, OllamaClient ollamaClient) {
        this.dbArticlesService = dbArticlesService;
        this.ollamaClient = ollamaClient;
    }

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody Map<String, String> requestBody) {
        try {
            String message = requestBody.get("message");
            String articleContent = null;

            // Check for specific Article UUID ---
            String uuidRegex =
                    "\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b";
            var uuidMatcher = Pattern.compile(uuidRegex).matcher(message);
            if (uuidMatcher.find()) {
                UUID articleId = UUID.fromString(uuidMatcher.group());
                Optional<JsonArticleReportResponse> article =
                        dbArticlesService.getArticleById(articleId);
                if (article.isPresent()) {
                    // TODO use clean full text here
                    articleContent = article.get().getDescription();
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Article with ID " + articleId + " not found.");
                }
            } else {
                // Check for specific Article URL ---
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

            if (articleContent == null) {

                // Embed the user's query using OllamaClient
                List<Double> queryVector = ollamaClient.embedQuery(message);

                // Search MongoDB for relevant articles
                List<ArticleContentEntity> relatedArticles =
                        dbArticlesService.findRelatedArticlesByVector(queryVector, 4);

                // Augment the prompt with retrieved content (Context)
                if (!relatedArticles.isEmpty()) {

                    // Create a context string from the retrieved article full text
                    String context =
                            relatedArticles.stream()
                                    .map(
                                            article ->
                                                    "--- Article: "
                                                            + article.getName()
                                                            + " ---\n"
                                                            + article.getCleanFullText())
                                    .collect(Collectors.joining("\n\n"));

                    // Construct the final RAG prompt
                    articleContent =
                            "You are an expert cybersecurity analyst. Answer the user's question"
                                + " using ONLY the context provided below. If the answer is not in"
                                + " the context, state 'I cannot answer this question based on the"
                                + " articles available.'\n\n"
                                + "CONTEXT:\n"
                                    + context
                                    + "\n\nUSER QUESTION: "
                                    + message;

                } else {
                    // Fallback: If no related articles found, ask the LLM the question directly
                    // (without context)
                    articleContent =
                            "Answer the following question about cybersecurity: " + message;
                }
            }

            String prompt = (articleContent != null) ? articleContent : message;

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
