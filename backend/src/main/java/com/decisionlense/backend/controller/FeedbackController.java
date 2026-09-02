

package com.decisionlense.backend.controller;

import com.decisionlense.backend.model.FeedbackRequest;
import com.decisionlense.backend.model.FeedbackResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(
        origins = {
                "http://localhost:5500",
                "http://127.0.0.1:5500"
        },
        allowCredentials = "true"
)
public class FeedbackController {

    private final List<FeedbackRecord> feedbackStore =
            new CopyOnWriteArrayList<>();

    @PostMapping
    public ResponseEntity<?> submitFeedback(
            @RequestBody FeedbackRequest request,
            Authentication authentication
    ) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            java.util.Map.of(
                                    "success", false,
                                    "message",
                                    "Authentication required."
                            )
                    );
        }

        if (request == null ||
                request.getFeedbackType() == null ||
                request.getFeedbackType().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            java.util.Map.of(
                                    "success", false,
                                    "message",
                                    "Feedback type is required."
                            )
                    );
        }

        String feedbackType =
                request.getFeedbackType()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (!List.of(
                "HELPFUL",
                "NOT_HELPFUL",
                "CORRECT",
                "INCORRECT",
                "CLARIFICATION_NEEDED"
        ).contains(feedbackType)) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            java.util.Map.of(
                                    "success", false,
                                    "message",
                                    "Unsupported feedback type."
                            )
                    );
        }

        String username =
                authentication.getName();

        String role =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse("UNKNOWN");

        FeedbackRecord record =
                new FeedbackRecord(
                        username,
                        role,
                        feedbackType,
                        safe(request.getComment()),
                        safe(request.getKpi()),
                        safe(request.getPrimaryDriver()),
                        request.getConfidence(),
                        request.getAbstained(),
                        Instant.now()
                );

        feedbackStore.add(record);

        System.out.println(
                "[FEEDBACK] user=" + username +
                " role=" + role +
                " type=" + feedbackType +
                " kpi=" + record.kpi +
                " driver=" + record.primaryDriver
        );

        return ResponseEntity.ok(
                new FeedbackResponse(
                        true,
                        "Feedback recorded successfully.",
                        feedbackType,
                        username,
                        role,
                        record.timestamp
                )
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<?> feedbackSummary(
            Authentication authentication
    ) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        long helpful =
                feedbackStore.stream()
                        .filter(
                                f -> "HELPFUL".equals(f.feedbackType)
                                        || "CORRECT".equals(f.feedbackType)
                        )
                        .count();

        long notHelpful =
                feedbackStore.stream()
                        .filter(
                                f -> "NOT_HELPFUL".equals(f.feedbackType)
                                        || "INCORRECT".equals(f.feedbackType)
                        )
                        .count();

        long clarification =
                feedbackStore.stream()
                        .filter(
                                f -> "CLARIFICATION_NEEDED"
                                        .equals(f.feedbackType)
                        )
                        .count();

        return ResponseEntity.ok(
                java.util.Map.of(
                        "totalFeedback",
                        feedbackStore.size(),

                        "helpfulOrCorrect",
                        helpful,

                        "notHelpfulOrIncorrect",
                        notHelpful,

                        "clarificationNeeded",
                        clarification
                )
        );
    }

    private String safe(String value) {

        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        if (cleaned.length() > 1000) {
            return cleaned.substring(0, 1000);
        }

        return cleaned;
    }

    private static class FeedbackRecord {

        private final String username;
        private final String role;
        private final String feedbackType;
        private final String comment;
        private final String kpi;
        private final String primaryDriver;
        private final Double confidence;
        private final Boolean abstained;
        private final Instant timestamp;

        private FeedbackRecord(
                String username,
                String role,
                String feedbackType,
                String comment,
                String kpi,
                String primaryDriver,
                Double confidence,
                Boolean abstained,
                Instant timestamp
        ) {
            this.username = username;
            this.role = role;
            this.feedbackType = feedbackType;
            this.comment = comment;
            this.kpi = kpi;
            this.primaryDriver = primaryDriver;
            this.confidence = confidence;
            this.abstained = abstained;
            this.timestamp = timestamp;
        }
    }
}