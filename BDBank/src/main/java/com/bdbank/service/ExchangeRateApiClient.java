package com.bdbank.service;

import com.bdbank.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class ExchangeRateApiClient {
    private static final String API_URL = "https://open.er-api.com/v6/latest/USD";

    public static double fetchUsdToBdtRate() {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                double rate = parseRateFromJson(response.body());
                System.out.println("[ExchangeRateApiClient] Live rate fetched from API: " + rate);
                return rate;
            }
            System.err.println("[ExchangeRateApiClient] API returned status " + response.statusCode() + ", using simulated feed.");
        } catch (Exception e) {
            System.err.println("[ExchangeRateApiClient] Live API unreachable (" + e.getMessage() + "), using simulated JSON feed.");
        }

        double simulatedRate = 118.50 + ThreadLocalRandom.current().nextDouble(-1.5, 1.5);
        String fakeApiJson = String.format(
                "{\"result\":\"success\",\"base_code\":\"USD\",\"rates\":{\"BDT\":%.4f,\"EUR\":0.92,\"GBP\":0.78}}",
                simulatedRate);
        return parseRateFromJson(fakeApiJson);
    }

    private static double parseRateFromJson(String jsonBody) {
        try {
            JsonNode root = JsonUtil.MAPPER.readTree(jsonBody);
            return root.get("rates").get("BDT").asDouble();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse exchange rate JSON: " + e.getMessage(), e);
        }
    }
}
