
package com.decisionlense.backend.model;

public class FeedbackRequest {

    private String feedbackType;
    private String comment;
    private String kpi;
    private String primaryDriver;
    private Double confidence;
    private Boolean abstained;

    public String getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getKpi() {
        return kpi;
    }

    public void setKpi(String kpi) {
        this.kpi = kpi;
    }

    public String getPrimaryDriver() {
        return primaryDriver;
    }

    public void setPrimaryDriver(String primaryDriver) {
        this.primaryDriver = primaryDriver;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Boolean getAbstained() {
        return abstained;
    }

    public void setAbstained(Boolean abstained) {
        this.abstained = abstained;
    }
}