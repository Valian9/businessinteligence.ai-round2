package com.decisionlense.backend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiSourceAnalysisResponse {

    private String kpi;
    private String movement;
    private double movementPercentage;

    private String primaryDriver;
    private String driverExplanation;

    private List<String> evidence = new ArrayList<>();

    private double confidence;
    private String analyticalMethod;

    private String recommendation;
    private String owner;
    private String monitoringPlan;

    // Driver movement / contribution signals
    private Map<String, Double> driverContributions;

    // New: normalized impact scores
    private Map<String, Double> driverImpactScores;

    // New: High / Medium / Low impact levels
    private Map<String, String> driverImpactLevels;
    private String persona;
    private String personaNarrative;
    private String personaAction; 
    private double totalRevenue;
    private double totalQuantity;
    private double totalStock;
    private double totalStockoutHours;
    private double totalMarketingSpend;
    private double totalConversions;
public String getPersona() {
    return persona;
}

public void setPersona(String persona) {
    this.persona = persona;
}

public String getPersonaNarrative() {
    return personaNarrative;
}

public void setPersonaNarrative(String personaNarrative) {
    this.personaNarrative = personaNarrative;
}

public String getPersonaAction() {
    return personaAction;
}

public void setPersonaAction(String personaAction) {
    this.personaAction = personaAction;
}
    public MultiSourceAnalysisResponse() {
    }

    // =========================================================
    // KPI
    // =========================================================

    public String getKpi() {
        return kpi;
    }

    public void setKpi(String kpi) {
        this.kpi = kpi;
    }

    public String getMovement() {
        return movement;
    }

    public void setMovement(String movement) {
        this.movement = movement;
    }

    public double getMovementPercentage() {
        return movementPercentage;
    }

    public void setMovementPercentage(double movementPercentage) {
        this.movementPercentage = movementPercentage;
    }

    // =========================================================
    // DRIVER
    // =========================================================

    public String getPrimaryDriver() {
        return primaryDriver;
    }

    public void setPrimaryDriver(String primaryDriver) {
        this.primaryDriver = primaryDriver;
    }

    public String getDriverExplanation() {
        return driverExplanation;
    }

    public void setDriverExplanation(String driverExplanation) {
        this.driverExplanation = driverExplanation;
    }

    // =========================================================
    // EVIDENCE
    // =========================================================

    public List<String> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<String> evidence) {
        this.evidence = evidence;
    }

    // =========================================================
    // CONFIDENCE / METHOD
    // =========================================================

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getAnalyticalMethod() {
        return analyticalMethod;
    }

    public void setAnalyticalMethod(String analyticalMethod) {
        this.analyticalMethod = analyticalMethod;
    }

    // =========================================================
    // ACTION
    // =========================================================

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getMonitoringPlan() {
        return monitoringPlan;
    }

    public void setMonitoringPlan(String monitoringPlan) {
        this.monitoringPlan = monitoringPlan;
    }

    // =========================================================
    // DRIVER CONTRIBUTIONS
    // =========================================================

    public Map<String, Double> getDriverContributions() {
        return driverContributions;
    }

    public void setDriverContributions(
            Map<String, Double> driverContributions
    ) {
        this.driverContributions = driverContributions;
    }

    // =========================================================
    // DRIVER IMPACT SCORES
    // =========================================================

    public Map<String, Double> getDriverImpactScores() {
        return driverImpactScores;
    }

    public void setDriverImpactScores(
            Map<String, Double> driverImpactScores
    ) {
        this.driverImpactScores = driverImpactScores;
    }

    // =========================================================
    // DRIVER IMPACT LEVELS
    // =========================================================

    public Map<String, String> getDriverImpactLevels() {
        return driverImpactLevels;
    }

    public void setDriverImpactLevels(
            Map<String, String> driverImpactLevels
    ) {
        this.driverImpactLevels = driverImpactLevels;
    }

    // =========================================================
    // TOTALS
    // =========================================================

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(double totalStock) {
        this.totalStock = totalStock;
    }

    public double getTotalStockoutHours() {
        return totalStockoutHours;
    }

    public void setTotalStockoutHours(
            double totalStockoutHours
    ) {
        this.totalStockoutHours = totalStockoutHours;
    }

    public double getTotalMarketingSpend() {
        return totalMarketingSpend;
    }

    public void setTotalMarketingSpend(
            double totalMarketingSpend
    ) {
        this.totalMarketingSpend = totalMarketingSpend;
    }

    public double getTotalConversions() {
        return totalConversions;
    }

    public void setTotalConversions(
            double totalConversions
    ) {
        this.totalConversions = totalConversions;
    }
}