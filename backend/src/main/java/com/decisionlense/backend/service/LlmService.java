
package com.decisionlense.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LlmService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private static final String OPENAI_URL =
            "https://api.openai.com/v1/responses";

    private static final String MODEL =
            "gpt-5.2";

    public LlmService(){

        this.objectMapper = new ObjectMapper();

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    // ============================================================
    // MAIN LLM METHOD
    // ============================================================

    public Map<String, Object> generateBusinessNarrative(
            Map<String, Object> analysisData) {

        Map<String, Object> fallback =
                buildFallback(analysisData);

        String apiKey =
                System.getenv("OPENAI_API_KEY");

        /*
         * API key nahi hai to CSV analysis fail nahi hoga.
         * Deterministic Java analysis continue karega.
         */

        if (apiKey == null || apiKey.isBlank()) {

            fallback.put(
                    "llmStatus",
                    "NOT_CONFIGURED"
            );

            fallback.put(
                    "llmMessage",
                    "OPENAI_API_KEY is not configured. Deterministic analysis was returned."
            );

            return fallback;
        }

        try {

            String prompt =
                    buildPrompt(analysisData);

            Map<String, Object> requestBody =
                    new LinkedHashMap<>();

            requestBody.put(
                    "model",
                    MODEL
            );

            requestBody.put(
                    "input",
                    prompt
            );

            requestBody.put(
                    "store",
                    false
            );

            String json =
                    objectMapper.writeValueAsString(
                            requestBody
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(OPENAI_URL))
                            .timeout(Duration.ofSeconds(60))
                            .header(
                                    "Authorization",
                                    "Bearer " + apiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(json)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            // ====================================================
            // CHECK API RESPONSE
            // ====================================================

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                fallback.put(
                        "llmStatus",
                        "ERROR"
                );

                fallback.put(
                        "llmMessage",
                        "LLM request failed with HTTP "
                                + response.statusCode()
                );

                System.out.println(
                        "LLM API Error: "
                                + response.body()
                );

                return fallback;
            }

            // ====================================================
            // READ RESPONSE
            // ====================================================

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            String outputText =
                    extractOutputText(root);

            if (outputText == null ||
                    outputText.isBlank()) {

                fallback.put(
                        "llmStatus",
                        "EMPTY_RESPONSE"
                );

                fallback.put(
                        "llmMessage",
                        "LLM returned no usable text."
                );

                return fallback;
            }

            // ====================================================
            // PARSE LLM JSON
            // ====================================================

            Map<String, Object> llmResult =
                    parseLlmJson(outputText);

            llmResult.put(
                    "llmStatus",
                    "SUCCESS"
            );

            llmResult.put(
                    "model",
                    MODEL
            );

            llmResult.put(
                    "quantitativeTruth",
                    "Deterministic Java calculations"
            );

            return llmResult;

        } catch (Exception e) {

            e.printStackTrace();

            fallback.put(
                    "llmStatus",
                    "ERROR"
            );

            fallback.put(
                    "llmMessage",
                    "LLM unavailable. Deterministic analysis was returned."
            );

            return fallback;
        }
    }

    // ============================================================
    // BUILD PROMPT
    // ============================================================

    private String buildPrompt(
            Map<String, Object> data) {

        String json;

        try {

            json =
                    objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(data);

        } catch (Exception e) {

            json =
                    data.toString();
        }

        return """
                You are the Business Intelligence AI layer
                inside DecisionLens AI.

                Your job is to transform deterministic business
                analytics into useful executive-level insights.

                IMPORTANT RULES:

                1. Never invent numbers.
                2. Never change calculated values.
                3. Never calculate a different total.
                4. Use ONLY the supplied analysis data.
                5. If evidence is weak, explicitly say so.
                6. Do not claim causation unless the data supports it.
                7. Recommendations must be practical.
                8. Mention uncertainty when appropriate.
                9. Keep the response concise and professional.
                10. Do not create facts that are not present in the data.

                Return ONLY valid JSON.

                Required JSON structure:

                {
                  "executiveInsight": "short executive summary",
                  "analystInsight": "analytical explanation",
                  "recommendation": "specific business recommendation",
                  "risk": "main business risk",
                  "opportunity": "main business opportunity",
                  "nextAction": "most useful next action",
                  "confidenceExplanation": "why the evidence is strong or weak"
                }

                ANALYSIS DATA:

                """ + json;
    }

    // ============================================================
    // EXTRACT OUTPUT TEXT
    // ============================================================

    private String extractOutputText(
            JsonNode root) {

        /*
         * Responses API output structure ko read karta hai.
         */

        JsonNode output =
                root.get("output");

        if (output != null &&
                output.isArray()) {

            for (JsonNode item : output) {

                JsonNode content =
                        item.get("content");

                if (content != null &&
                        content.isArray()) {

                    for (JsonNode contentItem :
                            content) {

                        if ("output_text".equals(
                                contentItem
                                        .path("type")
                                        .asText())) {

                            String text =
                                    contentItem
                                            .path("text")
                                            .asText();

                            if (!text.isBlank()) {

                                return text;
                            }
                        }
                    }
                }
            }
        }

        /*
         * Fallback response format.
         */

        JsonNode outputText =
                root.get("output_text");

        if (outputText != null &&
                outputText.isTextual()) {

            return outputText.asText();
        }

        return null;
    }

    // ============================================================
    // PARSE LLM JSON
    // ============================================================

    private Map<String, Object> parseLlmJson(
            String text) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        try {

            String cleaned =
                    text.trim();

            // ----------------------------------------------------
            // Remove markdown code fences
            // ----------------------------------------------------

            if (cleaned.startsWith("```json")) {

                cleaned =
                        cleaned.substring(7);

            } else if (cleaned.startsWith("```")) {

                cleaned =
                        cleaned.substring(3);
            }

            if (cleaned.endsWith("```")) {

                cleaned =
                        cleaned.substring(
                                0,
                                cleaned.length() - 3
                        );
            }

            cleaned =
                    cleaned.trim();

            JsonNode json =
                    objectMapper.readTree(
                            cleaned
                    );

            result.put(
                    "executiveInsight",
                    getText(
                            json,
                            "executiveInsight"
                    )
            );

            result.put(
                    "analystInsight",
                    getText(
                            json,
                            "analystInsight"
                    )
            );

            result.put(
                    "recommendation",
                    getText(
                            json,
                            "recommendation"
                    )
            );

            result.put(
                    "risk",
                    getText(
                            json,
                            "risk"
                    )
            );

            result.put(
                    "opportunity",
                    getText(
                            json,
                            "opportunity"
                    )
            );

            result.put(
                    "nextAction",
                    getText(
                            json,
                            "nextAction"
                    )
            );

            result.put(
                    "confidenceExplanation",
                    getText(
                            json,
                            "confidenceExplanation"
                    )
            );

        } catch (Exception e) {

            /*
             * Agar LLM malformed JSON bhej de,
             * backend crash nahi karega.
             */

            result.put(
                    "executiveInsight",
                    text
            );

            result.put(
                    "analystInsight",
                    text
            );

            result.put(
                    "recommendation",
                    "Review the analysed KPI and contributing segments before taking action."
            );

            result.put(
                    "risk",
                    "LLM output could not be structured."
            );

            result.put(
                    "opportunity",
                    "Use deterministic KPI results for further investigation."
            );

            result.put(
                    "nextAction",
                    "Review the strongest contributor and monitor the KPI over time."
            );

            result.put(
                    "confidenceExplanation",
                    "Structured LLM output was unavailable."
            );
        }

        return result;
    }

    // ============================================================
    // GET JSON TEXT
    // ============================================================

    private String getText(
            JsonNode node,
            String field) {

        JsonNode value =
                node.get(field);

        if (value == null ||
                value.isNull()) {

            return "";
        }

        return value.asText();
    }

    // ============================================================
    // FALLBACK
    // ============================================================

    private Map<String, Object> buildFallback(
            Map<String, Object> data) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "executiveInsight",
                String.valueOf(
                        data.getOrDefault(
                                "executiveInsight",
                                "Analysis completed using deterministic business calculations."
                        )
                )
        );

        result.put(
                "analystInsight",
                String.valueOf(
                        data.getOrDefault(
                                "analystInsight",
                                "Review the calculated KPIs and available evidence."
                        )
                )
        );

        result.put(
                "recommendation",
                String.valueOf(
                        data.getOrDefault(
                                "recommendation",
                                "Monitor the primary KPI and investigate the strongest contributor."
                        )
                )
        );

        result.put(
                "risk",
                "Limited evidence may reduce decision confidence."
        );

        result.put(
                "opportunity",
                "Use contributor-level analysis to identify improvement opportunities."
        );

        result.put(
                "nextAction",
                "Monitor KPI movement and contributor performance over time."
        );

        result.put(
                "confidenceExplanation",
                String.valueOf(
                        data.getOrDefault(
                                "confidenceStatus",
                                "Evidence-based deterministic analysis."
                        )
                )
        );

        return result;
    }
}

