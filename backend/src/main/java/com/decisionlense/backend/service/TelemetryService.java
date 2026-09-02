

package com.decisionlense.backend.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TelemetryService {

    private final AtomicLong totalRequests =
            new AtomicLong();

    private final AtomicLong successfulRequests =
            new AtomicLong();

    private final AtomicLong failedRequests =
            new AtomicLong();

    private final AtomicLong abstainedRequests =
            new AtomicLong();

    private final AtomicLong totalModelCalls =
            new AtomicLong();

    private final AtomicLong totalTokens =
            new AtomicLong();

    private final AtomicLong totalLatencyMs =
            new AtomicLong();

    private final AtomicLong estimatedCostMicros =
            new AtomicLong();

    public long startTimer() {
        return System.nanoTime();
    }

    public void recordRequest(
            long startNanos,
            boolean success,
            boolean abstained,
            long modelCalls,
            long tokens,
            double estimatedCost
    ) {

        long latencyMs =
                (System.nanoTime() - startNanos)
                        / 1_000_000L;

        totalRequests.incrementAndGet();

        if (success) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }

        if (abstained) {
            abstainedRequests.incrementAndGet();
        }

        totalModelCalls.addAndGet(modelCalls);
        totalTokens.addAndGet(tokens);
        totalLatencyMs.addAndGet(latencyMs);

        estimatedCostMicros.addAndGet(
                Math.round(estimatedCost * 1_000_000)
        );
    }

    public Map<String, Object> snapshot() {

        long requests =
                totalRequests.get();

        double averageLatency =
                requests == 0
                        ? 0
                        : (double) totalLatencyMs.get()
                        / requests;

        double averageModelCalls =
                requests == 0
                        ? 0
                        : (double) totalModelCalls.get()
                        / requests;

        double totalCost =
                estimatedCostMicros.get()
                        / 1_000_000.0;

        return Map.ofEntries(
                Map.entry("capturedAt", Instant.now().toString()),
                Map.entry("totalRequests", requests),
                Map.entry("successfulRequests", successfulRequests.get()),
                Map.entry("failedRequests", failedRequests.get()),
                Map.entry("abstainedRequests", abstainedRequests.get()),
                Map.entry("averageLatencyMs", round(averageLatency)),
                Map.entry("totalModelCalls", totalModelCalls.get()),
                Map.entry("averageModelCalls", round(averageModelCalls)),
                Map.entry("totalTokens", totalTokens.get()),
                Map.entry("estimatedCost", round(totalCost)),
                Map.entry("quantitativeEngine", "deterministic"),
                Map.entry("llmRole", "narrative_and_recommendation_only")
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}