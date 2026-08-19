package de.pharmaindex.ui;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class CatalogApiClient {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final String baseUrl;
    private final String apiKey;

    public CatalogApiClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
    }

    public DashboardDto dashboard() throws IOException, InterruptedException {
        return mapper.readValue(get(baseUrl + "/api/v1/ops/dashboard"), DashboardDto.class);
    }

    public PageResponse<ProductDto> search(String query) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(query == null ? "" : query, StandardCharsets.UTF_8);
        String uri = baseUrl + "/api/v1/products?size=50&q=" + encoded;
        return mapper.readValue(get(uri), mapper.getTypeFactory().constructParametricType(PageResponse.class, ProductDto.class));
    }

    public RevisionDto[] revisions(String pzn) throws IOException, InterruptedException {
        return mapper.readValue(get(baseUrl + "/api/v1/products/" + pzn + "/revisions"), RevisionDto[].class);
    }

    public MatchResponseDto match(String query) throws IOException, InterruptedException {
        String json = "{\"query\":" + mapper.writeValueAsString(query) + "}";
        HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/match"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)));
        return mapper.readValue(response.body(), MatchResponseDto.class);
    }

    public QualityFindingDto[] findings() throws IOException, InterruptedException {
        return mapper.readValue(get(baseUrl + "/api/v1/qa/findings"), QualityFindingDto[].class);
    }

    public String scan() throws IOException, InterruptedException {
        HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/qa/scan"))
                .POST(HttpRequest.BodyPublishers.noBody()));
        return response.body();
    }

    public void resolve(long id) throws IOException, InterruptedException {
        send(HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/qa/findings/" + id + "/resolve"))
                .POST(HttpRequest.BodyPublishers.noBody()));
    }

    private String get(String uri) throws IOException, InterruptedException {
        return send(HttpRequest.newBuilder(URI.create(uri)).GET()).body();
    }

    private HttpResponse<String> send(HttpRequest.Builder builder) throws IOException, InterruptedException {
        HttpRequest request = builder
                .header("X-API-Key", apiKey)
                .timeout(Duration.ofSeconds(20))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
        return response;
    }
}
