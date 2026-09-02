
package com.decisionlense.backend.model;

import java.time.Instant;

public class FeedbackResponse {

    private boolean success;
    private String message;
    private String feedbackType;
    private String username;
    private String role;
    private Instant timestamp;

    public FeedbackResponse() {
    }

    public FeedbackResponse(
            boolean success,
            String message,
            String feedbackType,
            String username,
            String role,
            Instant timestamp
    ) {
        this.success = success;
        this.message = message;
        this.feedbackType = feedbackType;
        this.username = username;
        this.role = role;
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}