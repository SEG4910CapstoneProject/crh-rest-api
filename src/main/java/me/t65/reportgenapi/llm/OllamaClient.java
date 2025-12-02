package me.t65.reportgenapi.llm;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class OllamaClient {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Configuration constants
    private static final String OLLAMA_BASE_URL = "http://ollama:11434";
    private static final String EMBEDDING_MODEL = "nomic-embed-text";

    /**
     * Converts a text query into an embedding vector using the Ollama API. Returns an empty list on
     * API failure (network, bad status, or parsing error). * @param text The user's query.
     *
     * @return A List of Doubles representing the 768-dimensional vector, or an empty list on
     *     failure.
     */
    public List<Double> embedQuery(String text) {

        try {
            String jsonRequest =
                    mapper.writeValueAsString(
                            Map.of(
                                    "model", EMBEDDING_MODEL,
                                    "input", text));

            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(OLLAMA_BASE_URL + "/api/embed"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                            .build();

            HttpResponse<String> response =
                    client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Ollama API failed: " + response.statusCode());
                System.err.println("BODY: " + response.body());
                return Collections.emptyList();
            }

            Map<String, Object> map = mapper.readValue(response.body(), Map.class);

            List<List<Double>> embeddings = (List<List<Double>>) map.get("embeddings");

            if (embeddings == null || embeddings.isEmpty()) {
                System.err.println("Ollama returned no embeddings.");
                return Collections.emptyList();
            }

            return embeddings.get(0);

        } catch (Exception e) {
            System.err.println("Embedding error: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
