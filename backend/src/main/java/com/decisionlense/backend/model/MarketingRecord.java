
package com.decisionlense.backend.model;

public class MarketingRecord {

    private String date;
    private String campaign;
    private String category;

    private double spend;
    private double impressions;
    private double clicks;
    private double conversions;

    public MarketingRecord() {
    }

    public MarketingRecord(String date, String campaign, String category,
                           double spend, double impressions,
                           double clicks, double conversions) {
        this.date = date;
        this.campaign = campaign;
        this.category = category;
        this.spend = spend;
        this.impressions = impressions;
        this.clicks = clicks;
        this.conversions = conversions;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getSpend() {
        return spend;
    }

    public void setSpend(double spend) {
        this.spend = spend;
    }

    public double getImpressions() {
        return impressions;
    }

    public void setImpressions(double impressions) {
        this.impressions = impressions;
    }

    public double getClicks() {
        return clicks;
    }

    public void setClicks(double clicks) {
        this.clicks = clicks;
    }

    public double getConversions() {
        return conversions;
    }

    public void setConversions(double conversions) {
        this.conversions = conversions;
    }
}