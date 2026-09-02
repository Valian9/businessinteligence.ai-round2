
package com.decisionlense.backend.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MultiSourceAnalysisService {

    private static final int MIN_COMMON_DATES = 7;
    private static final int MIN_SOURCE_RECORDS = 5;
    private static final int MIN_SOURCE_DATES = 5;
    private static final double ABSTENTION_THRESHOLD = 0.60;

    // =========================================================
    // DATA MODELS
    // =========================================================

    public static class SalesRecord {

        private final LocalDate date;
        private final String product;
        private final String category;
        private final String region;
        private final double quantity;
        private final double unitPrice;
        private final double cost;
        private final double revenue;

        public SalesRecord(
                LocalDate date,
                String product,
                String category,
                String region,
                double quantity,
                double unitPrice,
                double cost,
                double revenue
        ) {
            this.date = date;
            this.product = product;
            this.category = category;
            this.region = region;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.cost = cost;
            this.revenue = revenue;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getProduct() {
            return product;
        }

        public String getCategory() {
            return category;
        }

        public String getRegion() {
            return region;
        }

        public double getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public double getCost() {
            return cost;
        }

        public double getRevenue() {
            return revenue;
        }
    }

    public static class InventoryRecord {

        private final LocalDate date;
        private final String product;
        private final String category;
        private final double stockAvailable;
        private final double stockoutHours;
        private final double supplierDelay;

        public InventoryRecord(
                LocalDate date,
                String product,
                String category,
                double stockAvailable,
                double stockoutHours,
                double supplierDelay
        ) {
            this.date = date;
            this.product = product;
            this.category = category;
            this.stockAvailable = stockAvailable;
            this.stockoutHours = stockoutHours;
            this.supplierDelay = supplierDelay;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getProduct() {
            return product;
        }

        public String getCategory() {
            return category;
        }

        public double getStockAvailable() {
            return stockAvailable;
        }

        public double getStockoutHours() {
            return stockoutHours;
        }

        public double getSupplierDelay() {
            return supplierDelay;
        }
    }

    public static class MarketingRecord {

        private final LocalDate date;
        private final String campaign;
        private final String category;
        private final double spend;
        private final double impressions;
        private final double clicks;
        private final double conversions;

        public MarketingRecord(
                LocalDate date,
                String campaign,
                String category,
                double spend,
                double impressions,
                double clicks,
                double conversions
        ) {
            this.date = date;
            this.campaign = campaign;
            this.category = category;
            this.spend = spend;
            this.impressions = impressions;
            this.clicks = clicks;
            this.conversions = conversions;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getCampaign() {
            return campaign;
        }

        public String getCategory() {
            return category;
        }

        public double getSpend() {
            return spend;
        }

        public double getImpressions() {
            return impressions;
        }

        public double getClicks() {
            return clicks;
        }

        public double getConversions() {
            return conversions;
        }
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    public static class MultiSourceAnalysisResponse {

        private double totalRevenue;
        private double totalQuantity;

        /*
         * Kept as totalInventory for frontend backward compatibility.
         * This represents the sum of validated stock snapshot values,
         * not a unique physical inventory balance.
         */
        private double totalInventory;

        private double totalStockoutHours;
        private double marketingSpend;
        private double marketingConversions;

        private String kpi;
        private String movement;
        private double movementPercentage;

        private Map<String, Double> driverMovements =
                new LinkedHashMap<>();

        private Map<String, Double> driverImpactScores =
                new LinkedHashMap<>();

        private Map<String, String> driverImpactLevels =
                new LinkedHashMap<>();

        private String primaryDriver;

        private List<String> evidence =
                new ArrayList<>();

        private String driverExplanation;

        private String persona;
        private String personaNarrative;
        private String personaAction;

        private double confidence;
        private String confidenceLevel;

        private boolean abstained;
        private String clarificationRequest;

        private String analyticalMethod;
        private String recommendation;
        private String owner;
        private String monitoringPlan;

        private boolean sparseHistory;
        private int commonDateCount;
        private String historyCoverage;

        public double getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(double value) {
            this.totalRevenue = value;
        }

        public double getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(double value) {
            this.totalQuantity = value;
        }

        public double getTotalInventory() {
            return totalInventory;
        }

        public void setTotalInventory(double value) {
            this.totalInventory = value;
        }

        public double getTotalStockoutHours() {
            return totalStockoutHours;
        }

        public void setTotalStockoutHours(double value) {
            this.totalStockoutHours = value;
        }

        public double getMarketingSpend() {
            return marketingSpend;
        }

        public void setMarketingSpend(double value) {
            this.marketingSpend = value;
        }

        public double getMarketingConversions() {
            return marketingConversions;
        }

        public void setMarketingConversions(double value) {
            this.marketingConversions = value;
        }

        public String getKpi() {
            return kpi;
        }

        public void setKpi(String value) {
            this.kpi = value;
        }

        public String getMovement() {
            return movement;
        }

        public void setMovement(String value) {
            this.movement = value;
        }

        public double getMovementPercentage() {
            return movementPercentage;
        }

        public void setMovementPercentage(double value) {
            this.movementPercentage = value;
        }

        public Map<String, Double> getDriverMovements() {
            return driverMovements;
        }

        public void setDriverMovements(
                Map<String, Double> value
        ) {
            this.driverMovements = value;
        }

        public Map<String, Double> getDriverImpactScores() {
            return driverImpactScores;
        }

        public void setDriverImpactScores(
                Map<String, Double> value
        ) {
            this.driverImpactScores = value;
        }

        public Map<String, String> getDriverImpactLevels() {
            return driverImpactLevels;
        }

        public void setDriverImpactLevels(
                Map<String, String> value
        ) {
            this.driverImpactLevels = value;
        }

        public String getPrimaryDriver() {
            return primaryDriver;
        }

        public void setPrimaryDriver(String value) {
            this.primaryDriver = value;
        }

        public List<String> getEvidence() {
            return evidence;
        }

        public void setEvidence(List<String> value) {
            this.evidence = value;
        }

        public String getDriverExplanation() {
            return driverExplanation;
        }

        public void setDriverExplanation(String value) {
            this.driverExplanation = value;
        }

        public String getPersona() {
            return persona;
        }

        public void setPersona(String value) {
            this.persona = value;
        }

        public String getPersonaNarrative() {
            return personaNarrative;
        }

        public void setPersonaNarrative(String value) {
            this.personaNarrative = value;
        }

        public String getPersonaAction() {
            return personaAction;
        }

        public void setPersonaAction(String value) {
            this.personaAction = value;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double value) {
            this.confidence = value;
        }

        public String getConfidenceLevel() {
            return confidenceLevel;
        }

        public void setConfidenceLevel(String value) {
            this.confidenceLevel = value;
        }

        public boolean isAbstained() {
            return abstained;
        }

        public void setAbstained(boolean value) {
            this.abstained = value;
        }

        public String getClarificationRequest() {
            return clarificationRequest;
        }

        public void setClarificationRequest(String value) {
            this.clarificationRequest = value;
        }

        public String getAnalyticalMethod() {
            return analyticalMethod;
        }

        public void setAnalyticalMethod(String value) {
            this.analyticalMethod = value;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String value) {
            this.recommendation = value;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String value) {
            this.owner = value;
        }

        public String getMonitoringPlan() {
            return monitoringPlan;
        }

        public void setMonitoringPlan(String value) {
            this.monitoringPlan = value;
        }

        public boolean isSparseHistory() {
            return sparseHistory;
        }

        public void setSparseHistory(boolean value) {
            this.sparseHistory = value;
        }

        public int getCommonDateCount() {
            return commonDateCount;
        }

        public void setCommonDateCount(int value) {
            this.commonDateCount = value;
        }

        public String getHistoryCoverage() {
            return historyCoverage;
        }

        public void setHistoryCoverage(String value) {
            this.historyCoverage = value;
        }
    }

    // =========================================================
    // COLUMN TYPES
    // =========================================================

    private enum ColumnType {
        DATE,
        PRODUCT,
        CATEGORY,
        REGION,
        QUANTITY,
        UNIT_PRICE,
        COST,
        REVENUE,

        STOCK_AVAILABLE,
        STOCKOUT,
        SUPPLIER_DELAY,

        CAMPAIGN,
        SPEND,
        IMPRESSIONS,
        CLICKS,
        CONVERSIONS
    }

    private enum SourceType {
        SALES,
        INVENTORY,
        MARKETING
    }

    private static class ParsedDataSet {

        private final List<SalesRecord> sales;
        private final List<InventoryRecord> inventory;
        private final List<MarketingRecord> marketing;

        ParsedDataSet(
                List<SalesRecord> sales,
                List<InventoryRecord> inventory,
                List<MarketingRecord> marketing
        ) {
            this.sales = List.copyOf(sales);
            this.inventory = List.copyOf(inventory);
            this.marketing = List.copyOf(marketing);
        }
    }

    private static class SparseHistoryAssessment {

        boolean sparse;

        List<String> reasons =
                new ArrayList<>();
    }

    // =========================================================
    // MAIN ENTRY POINT
    // =========================================================

    public MultiSourceAnalysisResponse analyze(
            MultipartFile salesFile,
            MultipartFile inventoryFile,
            MultipartFile marketingFile,
            String persona
    ) throws IOException {

        if (salesFile == null || salesFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sales file is missing or empty."
            );
        }

        if (inventoryFile == null || inventoryFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "Inventory file is missing or empty."
            );
        }

        if (marketingFile == null || marketingFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "Marketing file is missing or empty."
            );
        }

        List<SalesRecord> sales =
                validateSalesRecords(
                        readSalesFile(salesFile)
                );

        List<InventoryRecord> inventory =
                validateInventoryRecords(
                        readInventoryFile(inventoryFile)
                );

        List<MarketingRecord> marketing =
                validateMarketingRecords(
                        readMarketingFile(marketingFile)
                );

        if (sales.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid Sales records found."
            );
        }

        if (inventory.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid Inventory records found."
            );
        }

        if (marketing.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid Marketing records found."
            );
        }

        ParsedDataSet validatedData =
                new ParsedDataSet(
                        sales,
                        inventory,
                        marketing
                );

        return buildAnalysis(
                validatedData.sales,
                validatedData.inventory,
                validatedData.marketing,
                safeText(
                        persona,
                        "Supply Chain Manager"
                )
        );
    }

    // =========================================================
    // ANALYSIS
    // =========================================================

    private MultiSourceAnalysisResponse buildAnalysis(
            List<SalesRecord> sales,
            List<InventoryRecord> inventory,
            List<MarketingRecord> marketing,
            String persona
    ) {

        MultiSourceAnalysisResponse response =
                new MultiSourceAnalysisResponse();

        // =====================================================
        // TOTALS
        // =====================================================

        double totalRevenue =
                sumSalesRevenue(sales);

        double totalQuantity =
                sumSalesQuantity(sales);

        double totalInventory =
                sumInventoryStock(inventory);

        double totalStockoutHours =
                sumStockoutHours(inventory);

        double marketingSpend =
                sumMarketingSpend(marketing);

        double marketingConversions =
                sumMarketingConversions(marketing);

        response.setTotalRevenue(
                round(totalRevenue)
        );

        response.setTotalQuantity(
                round(totalQuantity)
        );

        response.setTotalInventory(
                round(totalInventory)
        );

        response.setTotalStockoutHours(
                round(totalStockoutHours)
        );

        response.setMarketingSpend(
                round(marketingSpend)
        );

        /*
         * IMPORTANT:
         * This is calculated directly from the validated
         * MarketingRecord list.
         */
        response.setMarketingConversions(
                round(marketingConversions)
        );

        // =====================================================
        // SOURCE DATE SETS
        // =====================================================

        Set<LocalDate> salesDates =
                getDistinctDatesFromSales(sales);

        Set<LocalDate> inventoryDates =
                getDistinctDatesFromInventory(inventory);

        Set<LocalDate> marketingDates =
                getDistinctDatesFromMarketing(marketing);

        Set<LocalDate> commonDates =
                new TreeSet<>(salesDates);

        commonDates.retainAll(inventoryDates);
        commonDates.retainAll(marketingDates);

        response.setCommonDateCount(
                commonDates.size()
        );

        // =====================================================
        // SPARSE HISTORY
        // =====================================================

        SparseHistoryAssessment sparse =
                assessSparseHistory(
                        sales,
                        inventory,
                        marketing,
                        commonDates
                );

        response.setSparseHistory(
                sparse.sparse
        );

        // =====================================================
        // NOT ENOUGH COMMON HISTORY
        // =====================================================

        if (commonDates.size() < 2) {

            response.setKpi("Revenue");
            response.setMovement("Insufficient Data");
            response.setMovementPercentage(0.0);

            response.setPrimaryDriver(
                    "Insufficient aligned history"
            );

            response.setDriverExplanation(
                    "Sales, Inventory and Marketing data do not have enough "
                            + "common dates to calculate a reliable before-versus-after KPI movement."
            );

            response.setEvidence(
                    buildInsufficientHistoryEvidence(
                            sales,
                            inventory,
                            marketing,
                            commonDates,
                            sparse
                    )
            );

            response.setConfidence(0.0);
            response.setConfidenceLevel("Low");
            response.setAbstained(true);

            response.setClarificationRequest(
                    buildClarificationRequest(sparse)
            );

            response.setRecommendation(
                    "Do not automate a business decision until aligned historical data is available."
            );

            response.setOwner("Business Analyst");

            response.setMonitoringPlan(
                    "Validate source freshness, date alignment, KPI definitions and record completeness."
            );

            response.setAnalyticalMethod(
                    buildAnalyticalMethod()
            );

            applyPersonaInsight(
                    response,
                    persona,
                    0.0,
                    0.0,
                    0.0,
                    0.0
            );

            return response;
        }

        // =====================================================
        // PERIOD SPLIT
        // =====================================================

        List<LocalDate> sortedDates =
                new ArrayList<>(commonDates);

        sortedDates.sort(
                Comparator.naturalOrder()
        );

        /*
         * Use aligned observation dates rather than a raw calendar
         * midpoint. This gives a deterministic before/after split
         * even when source cadence contains gaps.
         *
         * Example:
         * 15 common dates -> 8 baseline + 7 recent
         */
        int splitIndex =
                Math.max(
                        1,
                        sortedDates.size() / 2
                );

        if (splitIndex >= sortedDates.size()) {
            splitIndex =
                    sortedDates.size() - 1;
        }

        Set<LocalDate> baselineDates =
                new TreeSet<>(
                        sortedDates.subList(
                                0,
                                splitIndex
                        )
                );

        Set<LocalDate> recentDates =
                new TreeSet<>(
                        sortedDates.subList(
                                splitIndex,
                                sortedDates.size()
                        )
                );

        LocalDate baselineStartDate =
                baselineDates.iterator().next();

        LocalDate baselineEndDate =
                ((TreeSet<LocalDate>) baselineDates).last();

        LocalDate recentStartDate =
                recentDates.iterator().next();

        LocalDate recentEndDate =
                ((TreeSet<LocalDate>) recentDates).last();

        // =====================================================
        // PERIOD RECORDS
        // =====================================================

        List<SalesRecord> baselineSales =
                filterSalesByDates(
                        sales,
                        baselineDates
                );

        List<SalesRecord> recentSales =
                filterSalesByDates(
                        sales,
                        recentDates
                );

        List<InventoryRecord> baselineInventory =
                filterInventoryByDates(
                        inventory,
                        baselineDates
                );

        List<InventoryRecord> recentInventory =
                filterInventoryByDates(
                        inventory,
                        recentDates
                );

        List<MarketingRecord> baselineMarketing =
                filterMarketingByDates(
                        marketing,
                        baselineDates
                );

        List<MarketingRecord> recentMarketing =
                filterMarketingByDates(
                        marketing,
                        recentDates
                );

        // =====================================================
        // PERIOD AGGREGATION
        // =====================================================

        double baselineRevenue =
                sumSalesRevenue(baselineSales);

        double recentRevenue =
                sumSalesRevenue(recentSales);

        double baselineQuantity =
                sumSalesQuantity(baselineSales);

        double recentQuantity =
                sumSalesQuantity(recentSales);

        double baselineInventoryValue =
                sumInventoryStock(
                        baselineInventory
                );

        double recentInventoryValue =
                sumInventoryStock(
                        recentInventory
                );

        double baselineStockout =
                sumStockoutHours(
                        baselineInventory
                );

        double recentStockout =
                sumStockoutHours(
                        recentInventory
                );

        double baselineConversions =
                sumMarketingConversions(
                        baselineMarketing
                );

        double recentConversions =
                sumMarketingConversions(
                        recentMarketing
                );

        double baselineSpend =
                sumMarketingSpend(
                        baselineMarketing
                );

        double recentSpend =
                sumMarketingSpend(
                        recentMarketing
                );

        // =====================================================
        // MOVEMENTS
        // =====================================================

        double revenueChange =
                percentageChange(
                        baselineRevenue,
                        recentRevenue
                );

        double quantityChange =
                percentageChange(
                        baselineQuantity,
                        recentQuantity
                );

        double inventoryChange =
                percentageChange(
                        baselineInventoryValue,
                        recentInventoryValue
                );

        double conversionChange =
                percentageChange(
                        baselineConversions,
                        recentConversions
                );

        double priceChange =
                percentageChange(
                        averageSellingPrice(
                                baselineSales
                        ),
                        averageSellingPrice(
                                recentSales
                        )
                );

        double stockoutDelta =
                percentageChange(
                        baselineStockout,
                        recentStockout
                );

        // =====================================================
        // KPI MOVEMENT
        // =====================================================

        response.setKpi("Revenue");

        response.setMovement(
                revenueChange > 2
                        ? "↑ Revenue Growth"
                        : revenueChange < -2
                        ? "↓ Revenue Decline"
                        : "→ Revenue Stable"
        );

        response.setMovementPercentage(
                round(revenueChange)
        );

        // =====================================================
        // DRIVER MOVEMENTS
        // =====================================================

        Map<String, Double> drivers =
                new LinkedHashMap<>();

        drivers.put(
                "Sales Volume",
                round(quantityChange)
        );

        drivers.put(
                "Average Price",
                round(priceChange)
        );

        drivers.put(
                "Inventory Availability",
                round(inventoryChange)
        );

        drivers.put(
                "Stockout Hours",
                round(stockoutDelta)
        );

        drivers.put(
                "Marketing Conversions",
                round(conversionChange)
        );

        response.setDriverMovements(
                drivers
        );

        // =====================================================
        // DRIVER IMPACT
        // =====================================================

        Map<String, Double> impactScores =
                new LinkedHashMap<>();

        impactScores.put(
                "Sales Volume",
                calculateImpactScore(
                        quantityChange,
                        false
                )
        );

        impactScores.put(
                "Average Price",
                calculateImpactScore(
                        priceChange,
                        false
                )
        );

        impactScores.put(
                "Inventory Availability",
                calculateImpactScore(
                        inventoryChange,
                        false
                )
        );

        impactScores.put(
                "Stockout Hours",
                calculateImpactScore(
                        stockoutDelta,
                        true
                )
        );

        impactScores.put(
                "Marketing Conversions",
                calculateImpactScore(
                        conversionChange,
                        false
                )
        );

        Map<String, Double> sortedImpact =
                sortByValueDescending(
                        impactScores
                );

        response.setDriverImpactScores(
                sortedImpact
        );

        Map<String, String> impactLevels =
                new LinkedHashMap<>();

        for (
                Map.Entry<String, Double> entry
                        : sortedImpact.entrySet()
        ) {

            impactLevels.put(
                    entry.getKey(),
                    impactLevel(
                            entry.getValue()
                    )
            );
        }

        response.setDriverImpactLevels(
                impactLevels
        );

        // =====================================================
        // PRIMARY DRIVER
        // =====================================================

        String primaryDriver =
                sortedImpact.entrySet()
                        .stream()
                        .filter(
                                entry ->
                                        entry.getValue() > 0
                        )
                        .findFirst()
                        .map(Map.Entry::getKey)
                        .orElse(
                                "Insufficient evidence"
                        );

        response.setPrimaryDriver(
                primaryDriver
        );

        // =====================================================
        // DRIVER EXPLANATION
        // =====================================================

        response.setDriverExplanation(
                buildDriverExplanation(
                        primaryDriver,
                        revenueChange,
                        quantityChange,
                        priceChange,
                        inventoryChange,
                        stockoutDelta,
                        conversionChange
                )
        );

        // =====================================================
        // EVIDENCE
        // =====================================================

        List<String> evidence =
                new ArrayList<>();

        evidence.add(
                "Baseline period: "
                        + baselineStartDate
                        + " to "
                        + baselineEndDate
        );

        evidence.add(
                "Recent period: "
                        + recentStartDate
                        + " to "
                        + recentEndDate
        );

        evidence.add(
                "Common-date aligned observations: "
                        + commonDates.size()
        );

        evidence.add(
                "Baseline aligned dates: "
                        + baselineDates.size()
                        + " | Recent aligned dates: "
                        + recentDates.size()
        );

        evidence.add(
                buildHistoryCoverage(
                        sales,
                        inventory,
                        marketing,
                        baselineSales,
                        recentSales,
                        baselineInventory,
                        recentInventory,
                        baselineMarketing,
                        recentMarketing
                )
        );

        // Explicit aggregate evidence
        evidence.add(
                "Marketing conversions: total="
                        + round(marketingConversions)
                        + " | baseline="
                        + round(baselineConversions)
                        + " | recent="
                        + round(recentConversions)
        );

        evidence.add(
                "Marketing spend: total="
                        + round(marketingSpend)
                        + " | baseline="
                        + round(baselineSpend)
                        + " | recent="
                        + round(recentSpend)
        );

        evidence.add(
                "Inventory stock snapshot sum: total="
                        + round(totalInventory)
                        + " | baseline="
                        + round(baselineInventoryValue)
                        + " | recent="
                        + round(recentInventoryValue)
        );

        addLargestRevenueDeclineEvidence(
                evidence,
                baselineSales,
                recentSales
        );

        addHighestStockoutEvidence(
                evidence,
                recentInventory
        );

        addMaximumSupplierDelayEvidence(
                evidence,
                recentInventory
        );

        addHighestCampaignConversionEvidence(
                evidence,
                recentMarketing
        );

        double recentMarketingEfficiency =
                recentSpend == 0.0
                        ? 0.0
                        : recentConversions / recentSpend;

        evidence.add(
                "Recent marketing conversion efficiency: "
                        + round(recentMarketingEfficiency)
                        + " conversions/currency unit"
        );

        if (sparse.sparse) {

            evidence.add(
                    "History sufficiency check failed: "
                            + String.join(
                                    "; ",
                                    sparse.reasons
                            )
            );

        } else {

            evidence.add(
                    "History sufficiency check passed: "
                            + "aligned history meets the minimum threshold."
            );
        }

        response.setEvidence(
                evidence
        );

        // =====================================================
        // CONFIDENCE
        // =====================================================

        double confidence =
                calculateConfidence(
                        sales,
                        inventory,
                        marketing,
                        commonDates.size(),
                        baselineSales.size(),
                        recentSales.size(),
                        baselineInventory.size(),
                        recentInventory.size(),
                        baselineMarketing.size(),
                        recentMarketing.size(),
                        quantityChange,
                        inventoryChange,
                        stockoutDelta,
                        conversionChange,
                        revenueChange,
                        sparse
                );

        response.setConfidence(
                round(confidence * 100.0)
        );

        response.setConfidenceLevel(
                confidence >= 0.75
                        ? "High"
                        : confidence >= 0.60
                        ? "Medium"
                        : "Low"
        );

        // =====================================================
        // ABSTENTION
        // =====================================================

        boolean abstain =
                sparse.sparse
                        || confidence < ABSTENTION_THRESHOLD
                        || "Insufficient evidence".equals(
                        primaryDriver
                );

        response.setAbstained(
                abstain
        );

        if (abstain) {

            response.setClarificationRequest(
                    buildClarificationRequest(
                            sparse
                    )
            );

            response.setRecommendation(
                    "Do not automate a business decision yet. "
                            + "Request clarification or additional aligned historical data."
            );

            response.setOwner(
                    "Business Analyst"
            );

            response.setMonitoringPlan(
                    "Monitor source completeness, freshness, date alignment, "
                            + "KPI definition and history depth."
            );

        } else {

            response.setClarificationRequest(
                    null
            );

            response.setRecommendation(
                    buildRecommendation(
                            primaryDriver,
                            revenueChange,
                            quantityChange,
                            inventoryChange,
                            stockoutDelta,
                            conversionChange
                    )
            );

            response.setOwner(
                    persona
            );

            response.setMonitoringPlan(
                    buildMonitoringPlan(
                            primaryDriver
                    )
            );
        }

        // =====================================================
        // PERSONA
        // =====================================================

        applyPersonaInsight(
                response,
                persona,
                revenueChange,
                inventoryChange,
                stockoutDelta,
                conversionChange
        );

        // =====================================================
        // ANALYTICAL METHOD
        // =====================================================

        response.setAnalyticalMethod(
                buildAnalyticalMethod()
        );

        // =====================================================
        // HISTORY COVERAGE
        // =====================================================

        response.setHistoryCoverage(
                buildHistoryCoverage(
                        sales,
                        inventory,
                        marketing,
                        baselineSales,
                        recentSales,
                        baselineInventory,
                        recentInventory,
                        baselineMarketing,
                        recentMarketing
                )
        );

        return response;
    }

    // =========================================================
    // SPARSE HISTORY
    // =========================================================

    private SparseHistoryAssessment assessSparseHistory(
            List<SalesRecord> sales,
            List<InventoryRecord> inventory,
            List<MarketingRecord> marketing,
            Set<LocalDate> commonDates
    ) {

        SparseHistoryAssessment result =
                new SparseHistoryAssessment();

        if (commonDates.size() < MIN_COMMON_DATES) {

            result.sparse = true;

            result.reasons.add(
                    "Only "
                            + commonDates.size()
                            + " common dates available; minimum required is "
                            + MIN_COMMON_DATES
            );
        }

        if (sales.size() < MIN_SOURCE_RECORDS) {

            result.sparse = true;

            result.reasons.add(
                    "Sales has only "
                            + sales.size()
                            + " valid records"
            );
        }

        if (inventory.size() < MIN_SOURCE_RECORDS) {

            result.sparse = true;

            result.reasons.add(
                    "Inventory has only "
                            + inventory.size()
                            + " valid records"
            );
        }

        if (marketing.size() < MIN_SOURCE_RECORDS) {

            result.sparse = true;

            result.reasons.add(
                    "Marketing has only "
                            + marketing.size()
                            + " valid records"
            );
        }

        long salesDateCount =
                sales.stream()
                        .map(SalesRecord::getDate)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();

        long inventoryDateCount =
                inventory.stream()
                        .map(InventoryRecord::getDate)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();

        long marketingDateCount =
                marketing.stream()
                        .map(MarketingRecord::getDate)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();

        if (salesDateCount < MIN_SOURCE_DATES) {

            result.sparse = true;

            result.reasons.add(
                    "Sales has fewer than "
                            + MIN_SOURCE_DATES
                            + " distinct dates"
            );
        }

        if (inventoryDateCount < MIN_SOURCE_DATES) {

            result.sparse = true;

            result.reasons.add(
                    "Inventory has fewer than "
                            + MIN_SOURCE_DATES
                            + " distinct dates"
            );
        }

        if (marketingDateCount < MIN_SOURCE_DATES) {

            result.sparse = true;

            result.reasons.add(
                    "Marketing has fewer than "
                            + MIN_SOURCE_DATES
                            + " distinct dates"
            );
        }

        return result;
    }

    // =========================================================
    // HISTORY COVERAGE
    // =========================================================

    private String buildHistoryCoverage(
            List<SalesRecord> sales,
            List<InventoryRecord> inventory,
            List<MarketingRecord> marketing,
            List<SalesRecord> baselineSales,
            List<SalesRecord> recentSales,
            List<InventoryRecord> baselineInventory,
            List<InventoryRecord> recentInventory,
            List<MarketingRecord> baselineMarketing,
            List<MarketingRecord> recentMarketing
    ) {

        Set<LocalDate> salesDates =
                getDistinctDatesFromSales(
                        sales
                );

        Set<LocalDate> inventoryDates =
                getDistinctDatesFromInventory(
                        inventory
                );

        Set<LocalDate> marketingDates =
                getDistinctDatesFromMarketing(
                        marketing
                );

        Set<LocalDate> commonDateSet =
                new TreeSet<>(salesDates);

        commonDateSet.retainAll(
                inventoryDates
        );

        commonDateSet.retainAll(
                marketingDates
        );

        return "History coverage: "
                + "Common aligned dates="
                + commonDateSet.size()
                + " | Sales records="
                + sales.size()
                + " | Inventory records="
                + inventory.size()
                + " | Marketing records="
                + marketing.size()
                + " | Sales dates="
                + salesDates.size()
                + " | Inventory dates="
                + inventoryDates.size()
                + " | Marketing dates="
                + marketingDates.size()
                + " | Baseline="
                + baselineSales.size()
                + "/"
                + baselineInventory.size()
                + "/"
                + baselineMarketing.size()
                + " | Recent="
                + recentSales.size()
                + "/"
                + recentInventory.size()
                + "/"
                + recentMarketing.size()
                + ".";
    }

    // =========================================================
    // PERSONA INSIGHTS
    // =========================================================

    private void applyPersonaInsight(
            MultiSourceAnalysisResponse response,
            String persona,
            double revenueChange,
            double inventoryChange,
            double stockoutDelta,
            double conversionChange
    ) {

        String normalized =
                safeText(
                        persona,
                        "Supply Chain Manager"
                );

        response.setPersona(
                normalized
        );

        if (
                normalized.equalsIgnoreCase(
                        "Marketing Manager"
                )
        ) {

            response.setPersonaNarrative(
                    String.format(
                            Locale.ROOT,
                            "Revenue changed by %.2f%% while marketing conversions changed by %.2f%%. Marketing performance should be reviewed alongside demand and supply signals before reallocating budget.",
                            revenueChange,
                            conversionChange
                    )
            );

            response.setPersonaAction(
                    "Prioritize campaigns with stronger conversion efficiency, review underperforming campaigns and coordinate demand signals with inventory availability."
            );

        } else if (
                normalized.equalsIgnoreCase(
                        "Executive"
                )
        ) {

            response.setPersonaNarrative(
                    String.format(
                            Locale.ROOT,
                            "Revenue changed by %.2f%% while inventory availability changed by %.2f%%, stockout exposure changed by %.2f%% and marketing conversions changed by %.2f%%. The evidence indicates a measurable cross-functional business movement requiring review.",
                            revenueChange,
                            inventoryChange,
                            stockoutDelta,
                            conversionChange
                    )
            );

            response.setPersonaAction(
                    "Review the highest-impact driver, assign an accountable owner and monitor leading operational and commercial indicators before making a strategic decision."
            );

        } else {

            response.setPersonaNarrative(
                    String.format(
                            Locale.ROOT,
                            "Revenue changed by %.2f%% while inventory availability changed by %.2f%% and stockout exposure changed by %.2f%%. Supply-side constraints are therefore a key area for operational review.",
                            revenueChange,
                            inventoryChange,
                            stockoutDelta
                    )
            );

            response.setPersonaAction(
                    "Prioritize replenishment of constrained products, investigate supplier delays, reduce stockout exposure and monitor inventory availability for revenue-critical products."
            );
        }
    }

    // =========================================================
    // RECOMMENDATION
    // =========================================================

    private String buildRecommendation(
            String primaryDriver,
            double revenueChange,
            double quantityChange,
            double inventoryChange,
            double stockoutDelta,
            double conversionChange
    ) {

        if ("Stockout Hours".equals(primaryDriver)) {

            return "Prioritize replenishment for stockout-prone products, investigate supplier delays, reduce stockout exposure and monitor revenue impact daily.";
        }

        if ("Inventory Availability".equals(primaryDriver)) {

            return "Review constrained inventory, prioritize revenue-critical products and investigate availability bottlenecks before demand is lost.";
        }

        if ("Marketing Conversions".equals(primaryDriver)) {

            return "Review campaigns with declining conversions, compare conversion efficiency against spend and reallocate budget toward stronger-performing campaigns.";
        }

        if ("Average Price".equals(primaryDriver)) {

            return "Review recent pricing changes, identify products with material price movement and validate whether price changes are affecting demand and revenue.";
        }

        if ("Sales Volume".equals(primaryDriver)) {

            return "Investigate demand changes by product and category, identify declining volume segments and coordinate corrective commercial actions.";
        }

        return "Review the KPI movement and validate the highest-impact driver before taking automated action.";
    }

    private String buildMonitoringPlan(
            String primaryDriver
    ) {

        return switch (primaryDriver) {

            case "Stockout Hours" ->
                    "Monitor stockout hours, stock availability, supplier delays and revenue-critical products daily.";

            case "Inventory Availability" ->
                    "Monitor available inventory, constrained SKUs, stockout exposure and replenishment lead time daily.";

            case "Marketing Conversions" ->
                    "Monitor campaign spend, conversions, conversion efficiency and campaign-level performance daily.";

            case "Average Price" ->
                    "Monitor average selling price, quantity movement and revenue by product/category.";

            case "Sales Volume" ->
                    "Monitor quantity, revenue and product-level demand trends daily.";

            default ->
                    "Monitor KPI movement, driver changes, source freshness and data quality.";
        };
    }

    // =========================================================
    // CONFIDENCE
    // =========================================================

    private double calculateConfidence(
            List<SalesRecord> sales,
            List<InventoryRecord> inventory,
            List<MarketingRecord> marketing,
            int commonDateCount,
            int baselineSalesCount,
            int recentSalesCount,
            int baselineInventoryCount,
            int recentInventoryCount,
            int baselineMarketingCount,
            int recentMarketingCount,
            double quantityChange,
            double inventoryChange,
            double stockoutChange,
            double conversionChange,
            double revenueChange,
            SparseHistoryAssessment sparse
    ) {

        double confidence = 0.40;

        if (sales.size() >= 10) {
            confidence += 0.04;
        }

        if (inventory.size() >= 10) {
            confidence += 0.04;
        }

        if (marketing.size() >= 10) {
            confidence += 0.04;
        }

        if (commonDateCount >= 7) {
            confidence += 0.10;
        }

        if (commonDateCount >= 14) {
            confidence += 0.05;
        }

        if (
                baselineSalesCount >= 2
                        && recentSalesCount >= 2
        ) {
            confidence += 0.03;
        }

        if (
                baselineInventoryCount >= 2
                        && recentInventoryCount >= 2
        ) {
            confidence += 0.03;
        }

        if (
                baselineMarketingCount >= 2
                        && recentMarketingCount >= 2
        ) {
            confidence += 0.03;
        }

        /*
         * Strong supply-side consistency:
         * inventory down + stockout exposure up.
         */
        boolean supplySignal =
                inventoryChange < -10
                        && stockoutChange > 10;

        boolean demandSignal =
                quantityChange < -5;

        boolean marketingSignal =
                conversionChange < -5;

        if (supplySignal) {
            confidence += 0.05;
        }

        if (
                supplySignal
                        && demandSignal
                        && revenueChange < -2
        ) {
            confidence += 0.06;
        }

        if (
                marketingSignal
                        && demandSignal
                        && revenueChange < -2
        ) {
            confidence += 0.04;
        }

        int activeSignals = 0;

        if (Math.abs(quantityChange) >= 5) {
            activeSignals++;
        }

        if (Math.abs(inventoryChange) >= 10) {
            activeSignals++;
        }

        if (Math.abs(stockoutChange) >= 10) {
            activeSignals++;
        }

        if (Math.abs(conversionChange) >= 5) {
            activeSignals++;
        }

        if (Math.abs(revenueChange) >= 5) {
            activeSignals++;
        }

        if (activeSignals == 0) {
            confidence -= 0.15;
        }

        if (activeSignals >= 3) {
            confidence += 0.04;
        }

        /*
         * Sparse history is a major confidence penalty.
         */
        if (sparse.sparse) {
            confidence -= 0.30;
        }

        /*
         * Prevent a misleadingly high confidence when
         * revenue itself is stable and driver movements
         * are weak.
         */
        if (
                Math.abs(revenueChange) < 2
                        && activeSignals <= 1
        ) {
            confidence -= 0.10;
        }

        return clamp(
                confidence,
                0.0,
                0.90
        );
    }

    // =========================================================
    // IMPACT
    // =========================================================

    private double calculateImpactScore(
            double change,
            boolean positiveChangeIsBad
    ) {

        if (!Double.isFinite(change)) {
            return 0.0;
        }

        boolean harmfulMovement;

        if (positiveChangeIsBad) {

            harmfulMovement =
                    change > 0;

        } else {

            harmfulMovement =
                    change < 0;
        }

        if (!harmfulMovement) {
            return 0.0;
        }

        double magnitude =
                Math.abs(change);

        double score =
                Math.min(
                        magnitude,
                        100.0
                );

        if (magnitude < 2) {

            score *= 0.35;

        } else if (magnitude < 5) {

            score *= 0.60;

        } else if (magnitude < 10) {

            score *= 0.80;
        }

        return round(
                clamp(
                        score,
                        0.0,
                        100.0
                )
        );
    }

    private String impactLevel(
            double score
    ) {

        if (score >= 50) {
            return "High";
        }

        if (score >= 25) {
            return "Medium";
        }

        return "Low";
    }

    private Map<String, Double> sortByValueDescending(
            Map<String, Double> source
    ) {

        return source.entrySet()
                .stream()
                .sorted(
                        Map.Entry
                                .<String, Double>comparingByValue()
                                .reversed()
                                .thenComparing(
                                        Map.Entry::getKey
                                )
                )
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (a, b) -> a,
                                LinkedHashMap::new
                        )
                );
    }

    // =========================================================
    // DRIVER EXPLANATION
    // =========================================================

    private String buildDriverExplanation(
            String primaryDriver,
            double revenueChange,
            double quantityChange,
            double priceChange,
            double inventoryChange,
            double stockoutDelta,
            double conversionChange
    ) {

        return String.format(
                Locale.ROOT,
                "Revenue changed by %.2f%%. Sales volume changed by %.2f%%, average price by %.2f%%, inventory availability by %.2f%%, stockout exposure by %.2f%% and marketing conversions by %.2f%%. %s is ranked highest by the movement-based driver prioritisation model. This is an explanatory prioritisation signal, not causal attribution.",
                revenueChange,
                quantityChange,
                priceChange,
                inventoryChange,
                stockoutDelta,
                conversionChange,
                primaryDriver
        );
    }

    // =========================================================
    // PRODUCT EVIDENCE
    // =========================================================

    private void addLargestRevenueDeclineEvidence(
            List<String> evidence,
            List<SalesRecord> baselineSales,
            List<SalesRecord> recentSales
    ) {

        Map<String, Double> baseline =
                baselineSales.stream()
                        .collect(
                                Collectors.groupingBy(
                                        r -> safeText(
                                                r.getProduct(),
                                                "Unknown Product"
                                        ),
                                        Collectors.summingDouble(
                                                SalesRecord::getRevenue
                                        )
                                )
                        );

        Map<String, Double> recent =
                recentSales.stream()
                        .collect(
                                Collectors.groupingBy(
                                        r -> safeText(
                                                r.getProduct(),
                                                "Unknown Product"
                                        ),
                                        Collectors.summingDouble(
                                                SalesRecord::getRevenue
                                        )
                                )
                        );

        String product = null;
        double worstChange = 0.0;

        for (String key : baseline.keySet()) {

            if (!recent.containsKey(key)) {
                continue;
            }

            double change =
                    percentageChange(
                            baseline.get(key),
                            recent.get(key)
                    );

            if (
                    change < worstChange
                            && Double.isFinite(change)
            ) {
                worstChange = change;
                product = key;
            }
        }

        if (product != null) {

            evidence.add(
                    product
                            + " largest revenue decline "
                            + round(worstChange)
                            + "%"
            );
        }
    }

    private void addHighestStockoutEvidence(
            List<String> evidence,
            List<InventoryRecord> inventory
    ) {

        inventory.stream()
                .filter(
                        r ->
                                Double.isFinite(
                                        r.getStockoutHours()
                                )
                )
                .max(
                        Comparator.comparingDouble(
                                InventoryRecord::getStockoutHours
                        )
                )
                .ifPresent(
                        record ->
                                evidence.add(
                                        "Highest stockout exposure "
                                                + safeText(
                                                record.getProduct(),
                                                "Unknown Product"
                                        )
                                                + ": "
                                                + round(
                                                record.getStockoutHours()
                                        )
                                                + " hours"
                                )
                );
    }

    private void addMaximumSupplierDelayEvidence(
            List<String> evidence,
            List<InventoryRecord> inventory
    ) {

        inventory.stream()
                .filter(
                        r ->
                                Double.isFinite(
                                        r.getSupplierDelay()
                                )
                )
                .max(
                        Comparator.comparingDouble(
                                InventoryRecord::getSupplierDelay
                        )
                )
                .ifPresent(
                        record ->
                                evidence.add(
                                        "Maximum supplier delay "
                                                + safeText(
                                                record.getProduct(),
                                                "Unknown Product"
                                        )
                                                + ": "
                                                + round(
                                                record.getSupplierDelay()
                                        )
                                                + " days"
                                )
                );
    }

    private void addHighestCampaignConversionEvidence(
            List<String> evidence,
            List<MarketingRecord> marketing
    ) {

        marketing.stream()
                .filter(
                        r ->
                                Double.isFinite(
                                        r.getConversions()
                                )
                )
                .max(
                        Comparator.comparingDouble(
                                MarketingRecord::getConversions
                        )
                )
                .ifPresent(
                        record ->
                                evidence.add(
                                        "Highest campaign conversions "
                                                + safeText(
                                                record.getCampaign(),
                                                "Unknown Campaign"
                                        )
                                                + ": "
                                                + round(
                                                record.getConversions()
                                        )
                                )
                );
    }

    // =========================================================
    // DATE FILTERS
    // =========================================================

    private List<SalesRecord> filterSalesByDates(
            List<SalesRecord> records,
            Set<LocalDate> dates
    ) {

        return records.stream()
                .filter(
                        r ->
                                r.getDate() != null
                                        && dates.contains(
                                        r.getDate()
                                )
                )
                .toList();
    }

    private List<InventoryRecord> filterInventoryByDates(
            List<InventoryRecord> records,
            Set<LocalDate> dates
    ) {

        return records.stream()
                .filter(
                        r ->
                                r.getDate() != null
                                        && dates.contains(
                                        r.getDate()
                                )
                )
                .toList();
    }

    private List<MarketingRecord> filterMarketingByDates(
            List<MarketingRecord> records,
            Set<LocalDate> dates
    ) {

        return records.stream()
                .filter(
                        r ->
                                r.getDate() != null
                                        && dates.contains(
                                        r.getDate()
                                )
                )
                .toList();
    }

    // =========================================================
    // DISTINCT DATE HELPERS
    // =========================================================

    private Set<LocalDate> getDistinctDatesFromSales(
            List<SalesRecord> records
    ) {

        return records.stream()
                .map(SalesRecord::getDate)
                .filter(Objects::nonNull)
                .collect(
                        Collectors.toCollection(
                                TreeSet::new
                        )
                );
    }

    private Set<LocalDate> getDistinctDatesFromInventory(
            List<InventoryRecord> records
    ) {

        return records.stream()
                .map(InventoryRecord::getDate)
                .filter(Objects::nonNull)
                .collect(
                        Collectors.toCollection(
                                TreeSet::new
                        )
                );
    }

    private Set<LocalDate> getDistinctDatesFromMarketing(
            List<MarketingRecord> records
    ) {

        return records.stream()
                .map(MarketingRecord::getDate)
                .filter(Objects::nonNull)
                .collect(
                        Collectors.toCollection(
                                TreeSet::new
                        )
                );
    }

    // =========================================================
    // FILE READING
    // =========================================================

    private List<SalesRecord> readSalesFile(
            MultipartFile file
    ) throws IOException {

        if (isExcel(file)) {
            return parseSalesExcel(file);
        }

        return parseSalesCsv(file);
    }

    private List<InventoryRecord> readInventoryFile(
            MultipartFile file
    ) throws IOException {

        if (isExcel(file)) {
            return parseInventoryExcel(file);
        }

        return parseInventoryCsv(file);
    }

    private List<MarketingRecord> readMarketingFile(
            MultipartFile file
    ) throws IOException {

        if (isExcel(file)) {
            return parseMarketingExcel(file);
        }

        return parseMarketingCsv(file);
    }

    private boolean isExcel(
            MultipartFile file
    ) {

        String name =
                safeText(
                        file.getOriginalFilename(),
                        ""
                ).toLowerCase(
                        Locale.ROOT
                );

        String contentType =
                safeText(
                        file.getContentType(),
                        ""
                ).toLowerCase(
                        Locale.ROOT
                );

        return name.endsWith(".xlsx")
                || name.endsWith(".xls")
                || contentType.contains(
                "spreadsheet"
        )
                || contentType.contains(
                "excel"
        );
    }

    // =========================================================
    // CSV PARSERS
    // =========================================================

    private List<SalesRecord> parseSalesCsv(
            MultipartFile file
    ) throws IOException {

        return parseCsv(
                file,
                SourceType.SALES
        );
    }

    private List<InventoryRecord> parseInventoryCsv(
            MultipartFile file
    ) throws IOException {

        return parseCsv(
                file,
                SourceType.INVENTORY
        );
    }

    private List<MarketingRecord> parseMarketingCsv(
            MultipartFile file
    ) throws IOException {

        return parseCsv(
                file,
                SourceType.MARKETING
        );
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> parseCsv(
            MultipartFile file,
            SourceType sourceType
    ) throws IOException {

        List<T> records =
                new ArrayList<>();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        file.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String headerLine =
                    reader.readLine();

            if (
                    headerLine == null
                            || headerLine.isBlank()
            ) {

                throw new IllegalArgumentException(
                        "CSV file has no header: "
                                + file.getOriginalFilename()
                );
            }

            String[] headers =
                    splitCsvLine(
                            headerLine
                    );

            Map<ColumnType, Integer> mapping =
                    detectColumns(
                            headers,
                            sourceType
                    );

            validateRequiredColumns(
                    mapping,
                    sourceType
            );

            String line;

            while (
                    (line = reader.readLine())
                            != null
            ) {

                if (line.isBlank()) {
                    continue;
                }

                try {

                    String[] columns =
                            splitCsvLine(line);

                    if (
                            sourceType
                                    == SourceType.SALES
                    ) {

                        LocalDate date =
                                parseDate(
                                        getValue(
                                                columns,
                                                mapping,
                                                ColumnType.DATE
                                        )
                                );

                        double quantity =
                                getRequiredDoubleValue(
                                        columns,
                                        mapping,
                                        ColumnType.QUANTITY
                                );

                        double revenue =
                                getRequiredDoubleValue(
                                        columns,
                                        mapping,
                                        ColumnType.REVENUE
                                );

                        records.add(
                                (T)
                                        new SalesRecord(
                                                date,
                                                getValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.PRODUCT
                                                ),
                                                getValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.CATEGORY
                                                ),
                                                getValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.REGION
                                                ),
                                                quantity,
                                                getDoubleValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.UNIT_PRICE
                                                ),
                                                getDoubleValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.COST
                                                ),
                                                revenue
                                        )
                        );

                    } else if (
                            sourceType
                                    == SourceType.INVENTORY
                    ) {

                        LocalDate date =
                                parseDate(
                                        getValue(
                                                columns,
                                                mapping,
                                                ColumnType.DATE
                                        )
                                );

                        double stock =
                                getRequiredDoubleValue(
                                        columns,
                                        mapping,
                                        ColumnType.STOCK_AVAILABLE
                                );

                        records.add(
                                (T)
                                        new InventoryRecord(
                                                date,
                                                getValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.PRODUCT
                                                ),
                                                getValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.CATEGORY
                                                ),
                                                stock,
                                                getDoubleValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.STOCKOUT
                                                ),
                                                getDoubleValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.SUPPLIER_DELAY
                                                )
                                        )
                        );

                    } else {

                        LocalDate date =
                                parseDate(
                                        getValue(
                                                columns,
                                                mapping,
                                                ColumnType.DATE
                                        )
                                );

                        double spend =
                                getRequiredDoubleValue(
                                        columns,
                                        mapping,
                                        ColumnType.SPEND
                                );

                        double conversions =
                                getRequiredDoubleValue(
                                        columns,
                                        mapping,
                                        ColumnType.CONVERSIONS
                                );

                        records.add(
                                (T)
                                        new MarketingRecord(
                                                date,
                                                getValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.CAMPAIGN
                                                ),
                                                getValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.CATEGORY
                                                ),
                                                spend,
                                                getDoubleValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.IMPRESSIONS
                                                ),
                                                getDoubleValue(
                                                        columns,
                                                        mapping,
                                                        ColumnType.CLICKS
                                                ),
                                                conversions
                                        )
                        );
                    }

                } catch (Exception ignored) {
                    /*
                     * Malformed row is rejected.
                     * Valid rows continue processing.
                     */
                }
            }
        }

        return records;
    }

    // =========================================================
    // EXCEL PARSERS
    // =========================================================

    private List<SalesRecord> parseSalesExcel(
            MultipartFile file
    ) throws IOException {

        List<SalesRecord> records =
                new ArrayList<>();

        try (
                InputStream input =
                        file.getInputStream();

                Workbook workbook =
                        WorkbookFactory.create(input)
        ) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "Excel workbook has no sheets."
                );
            }

            Sheet sheet =
                    workbook.getSheetAt(0);

            Iterator<Row> iterator =
                    sheet.iterator();

            if (!iterator.hasNext()) {
                throw new IllegalArgumentException(
                        "Excel sheet is empty."
                );
            }

            Row headerRow =
                    iterator.next();

            String[] headers =
                    excelRowToStrings(
                            headerRow
                    );

            Map<ColumnType, Integer> mapping =
                    detectColumns(
                            headers,
                            SourceType.SALES
                    );

            validateRequiredColumns(
                    mapping,
                    SourceType.SALES
            );

            while (iterator.hasNext()) {

                Row row =
                        iterator.next();

                if (isEmptyExcelRow(row)) {
                    continue;
                }

                try {

                    LocalDate date =
                            parseExcelDate(
                                    getExcelCell(
                                            row,
                                            mapping,
                                            ColumnType.DATE
                                    )
                            );

                    double quantity =
                            getRequiredExcelDouble(
                                    row,
                                    mapping,
                                    ColumnType.QUANTITY
                            );

                    double revenue =
                            getRequiredExcelDouble(
                                    row,
                                    mapping,
                                    ColumnType.REVENUE
                            );

                    records.add(
                            new SalesRecord(
                                    date,
                                    getExcelValue(
                                            row,
                                            mapping,
                                            ColumnType.PRODUCT
                                    ),
                                    getExcelValue(
                                            row,
                                            mapping,
                                            ColumnType.CATEGORY
                                    ),
                                    getExcelValue(
                                            row,
                                            mapping,
                                            ColumnType.REGION
                                    ),
                                    quantity,
                                    getExcelDouble(
                                            row,
                                            mapping,
                                            ColumnType.UNIT_PRICE
                                    ),
                                    getExcelDouble(
                                            row,
                                            mapping,
                                            ColumnType.COST
                                    ),
                                    revenue
                            )
                    );

                } catch (Exception ignored) {
                    // Reject malformed row.
                }
            }
        }

        return records;
    }

    private List<InventoryRecord> parseInventoryExcel(
            MultipartFile file
    ) throws IOException {

        List<InventoryRecord> records =
                new ArrayList<>();

        try (
                InputStream input =
                        file.getInputStream();

                Workbook workbook =
                        WorkbookFactory.create(input)
        ) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "Excel workbook has no sheets."
                );
            }

            Sheet sheet =
                    workbook.getSheetAt(0);

            Iterator<Row> iterator =
                    sheet.iterator();

            if (!iterator.hasNext()) {
                throw new IllegalArgumentException(
                        "Excel sheet is empty."
                );
            }

            Row headerRow =
                    iterator.next();

            String[] headers =
                    excelRowToStrings(
                            headerRow
                    );

            Map<ColumnType, Integer> mapping =
                    detectColumns(
                            headers,
                            SourceType.INVENTORY
                    );

            validateRequiredColumns(
                    mapping,
                    SourceType.INVENTORY
            );

            while (iterator.hasNext()) {

                Row row =
                        iterator.next();

                if (isEmptyExcelRow(row)) {
                    continue;
                }

                try {

                    LocalDate date =
                            parseExcelDate(
                                    getExcelCell(
                                            row,
                                            mapping,
                                            ColumnType.DATE
                                    )
                            );

                    double stock =
                            getRequiredExcelDouble(
                                    row,
                                    mapping,
                                    ColumnType.STOCK_AVAILABLE
                            );

                    records.add(
                            new InventoryRecord(
                                    date,
                                    getExcelValue(
                                            row,
                                            mapping,
                                            ColumnType.PRODUCT
                                    ),
                                    getExcelValue(
                                            row,
                                            mapping,
                                            ColumnType.CATEGORY
                                    ),
                                    stock,
                                    getExcelDouble(
                                            row,
                                            mapping,
                                            ColumnType.STOCKOUT
                                    ),
                                    getExcelDouble(
                                            row,
                                            mapping,
                                            ColumnType.SUPPLIER_DELAY
                                    )
                            )
                    );

                } catch (Exception ignored) {
                    // Reject malformed row.
                }
            }
        }

        return records;
    }

    private List<MarketingRecord> parseMarketingExcel(
            MultipartFile file
    ) throws IOException {

        List<MarketingRecord> records =
                new ArrayList<>();

        try (
                InputStream input =
                        file.getInputStream();

                Workbook workbook =
                        WorkbookFactory.create(input)
        ) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "Excel workbook has no sheets."
                );
            }

            Sheet sheet =
                    workbook.getSheetAt(0);

            Iterator<Row> iterator =
                    sheet.iterator();

            if (!iterator.hasNext()) {
                throw new IllegalArgumentException(
                        "Excel sheet is empty."
                );
            }

            Row headerRow =
                    iterator.next();

            String[] headers =
                    excelRowToStrings(
                            headerRow
                    );

            Map<ColumnType, Integer> mapping =
                    detectColumns(
                            headers,
                            SourceType.MARKETING
                    );

            validateRequiredColumns(
                    mapping,
                    SourceType.MARKETING
            );

            while (iterator.hasNext()) {

                Row row =
                        iterator.next();

                if (isEmptyExcelRow(row)) {
                    continue;
                }

                try {

                    LocalDate date =
                            parseExcelDate(
                                    getExcelCell(
                                            row,
                                            mapping,
                                            ColumnType.DATE
                                    )
                            );

                    double spend =
                            getRequiredExcelDouble(
                                    row,
                                    mapping,
                                    ColumnType.SPEND
                            );

                    double conversions =
                            getRequiredExcelDouble(
                                    row,
                                    mapping,
                                    ColumnType.CONVERSIONS
                            );

                    records.add(
                            new MarketingRecord(
                                    date,
                                    getExcelValue(
                                            row,
                                            mapping,
                                            ColumnType.CAMPAIGN
                                    ),
                                    getExcelValue(
                                            row,
                                            mapping,
                                            ColumnType.CATEGORY
                                    ),
                                    spend,
                                    getExcelDouble(
                                            row,
                                            mapping,
                                            ColumnType.IMPRESSIONS
                                    ),
                                    getExcelDouble(
                                            row,
                                            mapping,
                                            ColumnType.CLICKS
                                    ),
                                    conversions
                            )
                    );

                } catch (Exception ignored) {
                    // Reject malformed row.
                }
            }
        }

        return records;
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private List<SalesRecord> validateSalesRecords(
            List<SalesRecord> records
    ) {

        return records.stream()
                .filter(Objects::nonNull)
                .filter(
                        r ->
                                r.getDate() != null
                                        && Double.isFinite(
                                        r.getQuantity()
                                )
                                        && Double.isFinite(
                                        r.getRevenue()
                                )
                )
                .map(
                        r ->
                                new SalesRecord(
                                        r.getDate(),
                                        safeText(
                                                r.getProduct(),
                                                "Unknown Product"
                                        ),
                                        safeText(
                                                r.getCategory(),
                                                "Unknown Category"
                                        ),
                                        safeText(
                                                r.getRegion(),
                                                "Unknown Region"
                                        ),
                                        safeNumber(
                                                r.getQuantity()
                                        ),
                                        safeNumber(
                                                r.getUnitPrice()
                                        ),
                                        safeNumber(
                                                r.getCost()
                                        ),
                                        safeNumber(
                                                r.getRevenue()
                                        )
                                )
                )
                .toList();
    }

    private List<InventoryRecord> validateInventoryRecords(
            List<InventoryRecord> records
    ) {

        return records.stream()
                .filter(Objects::nonNull)
                .filter(
                        r ->
                                r.getDate() != null
                                        && Double.isFinite(
                                        r.getStockAvailable()
                                )
                )
                .map(
                        r ->
                                new InventoryRecord(
                                        r.getDate(),
                                        safeText(
                                                r.getProduct(),
                                                "Unknown Product"
                                        ),
                                        safeText(
                                                r.getCategory(),
                                                "Unknown Category"
                                        ),
                                        safeNumber(
                                                r.getStockAvailable()
                                        ),
                                        safeNumber(
                                                r.getStockoutHours()
                                        ),
                                        safeNumber(
                                                r.getSupplierDelay()
                                        )
                                )
                )
                .toList();
    }

    private List<MarketingRecord> validateMarketingRecords(
            List<MarketingRecord> records
    ) {

        return records.stream()
                .filter(Objects::nonNull)
                .filter(
                        r ->
                                r.getDate() != null
                                        && Double.isFinite(
                                        r.getSpend()
                                )
                                        && Double.isFinite(
                                        r.getConversions()
                                )
                )
                .map(
                        r ->
                                new MarketingRecord(
                                        r.getDate(),
                                        safeText(
                                                r.getCampaign(),
                                                "Unknown Campaign"
                                        ),
                                        safeText(
                                                r.getCategory(),
                                                "Unknown Category"
                                        ),
                                        safeNumber(
                                                r.getSpend()
                                        ),
                                        safeNumber(
                                                r.getImpressions()
                                        ),
                                        safeNumber(
                                                r.getClicks()
                                        ),
                                        safeNumber(
                                                r.getConversions()
                                        )
                                )
                )
                .toList();
    }

    // =========================================================
    // HEADER DETECTION
    // =========================================================

    private Map<ColumnType, Integer> detectColumns(
            String[] headers,
            SourceType sourceType
    ) {

        Map<ColumnType, Integer> mapping =
                new EnumMap<>(
                        ColumnType.class
                );

        for (int i = 0; i < headers.length; i++) {

            String normalized =
                    normalizeHeader(
                            headers[i]
                    );

            if (normalized.isBlank()) {
                continue;
            }

            ColumnType detected =
                    detectColumnType(
                            normalized,
                            sourceType
                    );

            if (
                    detected != null
                            && !mapping.containsKey(
                            detected
                    )
            ) {

                mapping.put(
                        detected,
                        i
                );
            }
        }

        return mapping;
    }

    private ColumnType detectColumnType(
            String header,
            SourceType sourceType
    ) {

        // =====================================================
        // DATE
        // =====================================================

        if (
                matches(
                        header,
                        "date",
                        "day",
                        "datetime",
                        "timestamp",
                        "salesdate",
                        "inventorydate",
                        "marketingdate",
                        "transactiondate"
                )
        ) {
            return ColumnType.DATE;
        }

        // =====================================================
        // SALES
        // =====================================================

        if (sourceType == SourceType.SALES) {

            if (
                    matches(
                            header,
                            "product",
                            "productid",
                            "productname",
                            "sku",
                            "item",
                            "itemid"
                    )
            ) {
                return ColumnType.PRODUCT;
            }

            if (
                    matches(
                            header,
                            "category",
                            "productcategory",
                            "categoryname"
                    )
            ) {
                return ColumnType.CATEGORY;
            }

            if (
                    matches(
                            header,
                            "region",
                            "area",
                            "territory",
                            "market"
                    )
            ) {
                return ColumnType.REGION;
            }

            if (
                    matches(
                            header,
                            "quantity",
                            "qty",
                            "units",
                            "unitssold",
                            "salesquantity",
                            "volume"
                    )
            ) {
                return ColumnType.QUANTITY;
            }

            if (
                    matches(
                            header,
                            "unitprice",
                            "price",
                            "sellingprice",
                            "sellingunitprice",
                            "avgprice"
                    )
            ) {
                return ColumnType.UNIT_PRICE;
            }

            if (
                    matches(
                            header,
                            "cost",
                            "unitcost",
                            "productcost",
                            "costprice"
                    )
            ) {
                return ColumnType.COST;
            }

            if (
                    matches(
                            header,
                            "revenue",
                            "salesrevenue",
                            "totalrevenue",
                            "amount",
                            "salesamount",
                            "totalamount"
                    )
            ) {
                return ColumnType.REVENUE;
            }

            if (header.contains("revenue")) {
                return ColumnType.REVENUE;
            }

            if (
                    header.contains("quantity")
                            || header.contains("qty")
            ) {
                return ColumnType.QUANTITY;
            }

            if (
                    header.contains("unitprice")
                            || header.equals("price")
            ) {
                return ColumnType.UNIT_PRICE;
            }
        }

        // =====================================================
        // INVENTORY
        // =====================================================

        if (sourceType == SourceType.INVENTORY) {

            /*
             * CRITICAL:
             * stockout checked BEFORE stock.
             *
             * Therefore:
             * stockout_hours -> STOCKOUT
             * stock_available -> STOCK_AVAILABLE
             */
            if (
                    matches(
                            header,
                            "stockouthours",
                            "stockouthour",
                            "stockout",
                            "outofstockhours",
                            "outofstock",
                            "stockoutduration",
                            "stockouttime"
                    )
            ) {
                return ColumnType.STOCKOUT;
            }

            if (
                    header.contains("stockout")
                            || header.contains("outofstock")
            ) {
                return ColumnType.STOCKOUT;
            }

            if (
                    matches(
                            header,
                            "supplierdelay",
                            "supplierdelaydays",
                            "supplierdelayday",
                            "deliverydelay",
                            "leadtime",
                            "supplierleadtime",
                            "deliverydelaydays"
                    )
            ) {
                return ColumnType.SUPPLIER_DELAY;
            }

            if (
                    matches(
                            header,
                            "stock",
                            "stockavailable",
                            "availablestock",
                            "stockavailableunits",
                            "inventory",
                            "inventorylevel",
                            "stocklevel",
                            "onhand",
                            "onhandstock",
                            "availableinventory",
                            "availableinventoryunits",
                            "currentstock",
                            "endinginventory",
                            "closingstock"
                    )
            ) {
                return ColumnType.STOCK_AVAILABLE;
            }

            if (
                    matches(
                            header,
                            "product",
                            "productid",
                            "productname",
                            "sku",
                            "item",
                            "itemid"
                    )
            ) {
                return ColumnType.PRODUCT;
            }

            if (
                    matches(
                            header,
                            "category",
                            "productcategory",
                            "categoryname"
                    )
            ) {
                return ColumnType.CATEGORY;
            }

            if (
                    header.contains("supplier")
                            && header.contains("delay")
            ) {
                return ColumnType.SUPPLIER_DELAY;
            }

            /*
             * Avoid interpreting stockout_hours as stock.
             */
            if (
                    (header.contains("stock")
                            || header.contains("inventory"))
                            && !header.contains("out")
            ) {
                return ColumnType.STOCK_AVAILABLE;
            }
        }

        // =====================================================
        // MARKETING
        // =====================================================

        if (sourceType == SourceType.MARKETING) {

            if (
                    matches(
                            header,
                            "campaign",
                            "campaignname",
                            "campaignid",
                            "adcampaign",
                            "marketingcampaign"
                    )
            ) {
                return ColumnType.CAMPAIGN;
            }

            if (
                    matches(
                            header,
                            "category",
                            "productcategory",
                            "categoryname"
                    )
            ) {
                return ColumnType.CATEGORY;
            }

            if (
                    matches(
                            header,
                            "spend",
                            "marketingspend",
                            "adspend",
                            "advertisingspend",
                            "campaignspend",
                            "marketingcost",
                            "advertisingcost",
                            "spendamount",
                            "adcost",
                            "budgetspent",
                            "totalspend",
                            "totalmarketingspend",
                            "marketinginvestment"
                    )
            ) {
                return ColumnType.SPEND;
            }

            if (
                    matches(
                            header,
                            "impressions",
                            "impression",
                            "views",
                            "adimpressions"
                    )
            ) {
                return ColumnType.IMPRESSIONS;
            }

            if (
                    matches(
                            header,
                            "clicks",
                            "click",
                            "adclicks"
                    )
            ) {
                return ColumnType.CLICKS;
            }

            /*
             * IMPORTANT:
             * Explicit conversion aliases only.
             *
             * We intentionally do NOT match:
             * conversionrate
             * conversionpercentage
             * conversionratio
             *
             * because those are ratios, not conversion counts.
             */
            if (
                    matches(
                            header,
                            "conversions",
                            "conversion",
                            "conversioncount",
                            "conversioncounts",
                            "conversionvolume",
                            "leads",
                            "leadcount",
                            "orders",
                            "ordercount",
                            "signups",
                            "signupcount",
                            "purchases",
                            "purchasecount",
                            "convertedusers",
                            "totalconversions"
                    )
            ) {
                return ColumnType.CONVERSIONS;
            }

            if (
                    header.contains("conversion")
                            && !header.contains("rate")
                            && !header.contains("ratio")
                            && !header.contains("percent")
            ) {
                return ColumnType.CONVERSIONS;
            }

            if (header.contains("spend")) {
                return ColumnType.SPEND;
            }

            if (header.contains("campaign")) {
                return ColumnType.CAMPAIGN;
            }

            if (header.contains("impression")) {
                return ColumnType.IMPRESSIONS;
            }

            if (header.contains("click")) {
                return ColumnType.CLICKS;
            }
        }

        return null;
    }

    private boolean matches(
            String header,
            String... aliases
    ) {

        for (String alias : aliases) {

            if (header.equals(alias)) {
                return true;
            }
        }

        return false;
    }

    private void validateRequiredColumns(
            Map<ColumnType, Integer> mapping,
            SourceType sourceType
    ) {

        requireColumn(
                mapping,
                ColumnType.DATE
        );

        if (sourceType == SourceType.SALES) {

            requireColumn(
                    mapping,
                    ColumnType.QUANTITY
            );

            requireColumn(
                    mapping,
                    ColumnType.REVENUE
            );

        } else if (
                sourceType == SourceType.INVENTORY
        ) {

            requireColumn(
                    mapping,
                    ColumnType.STOCK_AVAILABLE
            );

        } else {

            requireColumn(
                    mapping,
                    ColumnType.SPEND
            );

            requireColumn(
                    mapping,
                    ColumnType.CONVERSIONS
            );
        }
    }

    private void requireColumn(
            Map<ColumnType, Integer> mapping,
            ColumnType type
    ) {

        if (!mapping.containsKey(type)) {

            throw new IllegalArgumentException(
                    "Required column not detected: "
                            + type
            );
        }
    }

    // =========================================================
    // CSV HELPERS
    // =========================================================

    private String[] splitCsvLine(
            String line
    ) {

        if (line == null) {
            return new String[0];
        }

        List<String> values =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean insideQuotes = false;

        for (
                int i = 0;
                i < line.length();
                i++
        ) {

            char c =
                    line.charAt(i);

            if (c == '"') {

                if (
                        insideQuotes
                                && i + 1 < line.length()
                                && line.charAt(i + 1) == '"'
                ) {

                    current.append('"');
                    i++;

                } else {

                    insideQuotes =
                            !insideQuotes;
                }

            } else if (
                    c == ','
                            && !insideQuotes
            ) {

                values.add(
                        current.toString()
                );

                current.setLength(0);

            } else {

                current.append(c);
            }
        }

        values.add(
                current.toString()
        );

        return values.toArray(
                new String[0]
        );
    }

    private String normalizeHeader(
            String header
    ) {

        if (header == null) {
            return "";
        }

        return header
                .replace("\uFEFF", "")
                .replace("\u00A0", " ")
                .trim()
                .toLowerCase(
                        Locale.ROOT
                )
                .replaceAll(
                        "[^a-z0-9]",
                        ""
                );
    }

    private String getValue(
            String[] columns,
            Map<ColumnType, Integer> mapping,
            ColumnType type
    ) {

        Integer index =
                mapping.get(type);

        if (
                index == null
                        || index < 0
                        || index >= columns.length
        ) {
            return "";
        }

        return cleanText(
                columns[index]
        );
    }

    private double getDoubleValue(
            String[] columns,
            Map<ColumnType, Integer> mapping,
            ColumnType type
    ) {

        String value =
                getValue(
                        columns,
                        mapping,
                        type
                );

        if (isMissingValue(value)) {
            return 0.0;
        }

        return parseDouble(value);
    }

    private double getRequiredDoubleValue(
            String[] columns,
            Map<ColumnType, Integer> mapping,
            ColumnType type
    ) {

        String value =
                getValue(
                        columns,
                        mapping,
                        type
                );

        if (isMissingValue(value)) {

            throw new IllegalArgumentException(
                    "Missing required numeric value: "
                            + type
            );
        }

        return parseDouble(value);
    }

    // =========================================================
    // EXCEL HELPERS
    // =========================================================

    private String[] excelRowToStrings(
            Row row
    ) {

        int lastCell =
                Math.max(
                        row.getLastCellNum(),
                        0
                );

        String[] values =
                new String[lastCell];

        DataFormatter formatter =
                new DataFormatter();

        for (
                int i = 0;
                i < lastCell;
                i++
        ) {

            Cell cell =
                    row.getCell(
                            i,
                            Row.MissingCellPolicy
                                    .RETURN_BLANK_AS_NULL
                    );

            values[i] =
                    cell == null
                            ? ""
                            : cleanText(
                            formatter.formatCellValue(
                                    cell
                            )
                    );
        }

        return values;
    }

    private boolean isEmptyExcelRow(
            Row row
    ) {

        if (row == null) {
            return true;
        }

        DataFormatter formatter =
                new DataFormatter();

        short lastCell =
                row.getLastCellNum();

        if (lastCell <= 0) {
            return true;
        }

        for (
                int i = 0;
                i < lastCell;
                i++
        ) {

            Cell cell =
                    row.getCell(
                            i,
                            Row.MissingCellPolicy
                                    .RETURN_BLANK_AS_NULL
                    );

            if (
                    cell != null
                            && !formatter
                            .formatCellValue(cell)
                            .trim()
                            .isEmpty()
            ) {

                return false;
            }
        }

        return true;
    }

    private String getExcelCell(
            Row row,
            Map<ColumnType, Integer> mapping,
            ColumnType type
    ) {

        Integer index =
                mapping.get(type);

        if (index == null) {
            return "";
        }

        Cell cell =
                row.getCell(
                        index,
                        Row.MissingCellPolicy
                                .RETURN_BLANK_AS_NULL
                );

        if (cell == null) {
            return "";
        }

        if (
                cell.getCellType()
                        == CellType.NUMERIC
        ) {

            if (
                    DateUtil.isCellDateFormatted(
                            cell
                    )
            ) {

                return cell
                        .getLocalDateTimeCellValue()
                        .toLocalDate()
                        .toString();
            }

            return Double.toString(
                    cell.getNumericCellValue()
            );
        }

        if (
                cell.getCellType()
                        == CellType.FORMULA
        ) {

            if (
                    cell.getCachedFormulaResultType()
                            == CellType.NUMERIC
            ) {

                return Double.toString(
                        cell.getNumericCellValue()
                );
            }
        }

        DataFormatter formatter =
                new DataFormatter();

        return cleanText(
                formatter.formatCellValue(
                        cell
                )
        );
    }

    private String getExcelValue(
            Row row,
            Map<ColumnType, Integer> mapping,
            ColumnType type
    ) {

        return getExcelCell(
                row,
                mapping,
                type
        );
    }

    private double getExcelDouble(
            Row row,
            Map<ColumnType, Integer> mapping,
            ColumnType type
    ) {

        String value =
                getExcelCell(
                        row,
                        mapping,
                        type
                );

        if (isMissingValue(value)) {
            return 0.0;
        }

        return parseDouble(value);
    }

    private double getRequiredExcelDouble(
            Row row,
            Map<ColumnType, Integer> mapping,
            ColumnType type
    ) {

        String value =
                getExcelCell(
                        row,
                        mapping,
                        type
                );

        if (isMissingValue(value)) {

            throw new IllegalArgumentException(
                    "Missing required Excel numeric value: "
                            + type
            );
        }

        return parseDouble(value);
    }

    // =========================================================
    // DATE PARSING
    // =========================================================

    private LocalDate parseDate(
            String value
    ) {

        if (isMissingValue(value)) {

            throw new IllegalArgumentException(
                    "Date is missing."
            );
        }

        String cleaned =
                cleanText(value);

        List<DateTimeFormatter> formats =
                List.of(
                        DateTimeFormatter.ISO_LOCAL_DATE,

                        DateTimeFormatter.ofPattern(
                                "dd-MM-yyyy"
                        ),

                        DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy"
                        ),

                        DateTimeFormatter.ofPattern(
                                "MM/dd/yyyy"
                        ),

                        DateTimeFormatter.ofPattern(
                                "MM-dd-yyyy"
                        ),

                        DateTimeFormatter.ofPattern(
                                "yyyy/MM/dd"
                        ),

                        DateTimeFormatter.ofPattern(
                                "dd MMM yyyy",
                                Locale.ENGLISH
                        ),

                        DateTimeFormatter.ofPattern(
                                "MMM dd yyyy",
                                Locale.ENGLISH
                        )
                );

        for (
                DateTimeFormatter formatter
                        : formats
        ) {

            try {

                return LocalDate.parse(
                        cleaned,
                        formatter
                );

            } catch (
                    DateTimeParseException ignored
            ) {
            }
        }

        /*
         * Support common Excel/ISO datetime strings.
         */
        if (cleaned.contains("T")) {

            try {

                return LocalDate.parse(
                        cleaned.substring(
                                0,
                                cleaned.indexOf("T")
                        )
                );

            } catch (Exception ignored) {
            }
        }

        if (cleaned.contains(" ")) {

            try {

                return LocalDate.parse(
                        cleaned.substring(
                                0,
                                cleaned.indexOf(" ")
                        )
                );

            } catch (Exception ignored) {
            }
        }

        throw new IllegalArgumentException(
                "Unsupported date format: "
                        + value
        );
    }

    private LocalDate parseExcelDate(
            String value
    ) {

        return parseDate(value);
    }

    // =========================================================
    // NUMBER PARSING
    // =========================================================

    private double parseDouble(
            String value
    ) {

        if (isMissingValue(value)) {

            throw new IllegalArgumentException(
                    "Numeric value is missing."
            );
        }

        String cleaned =
                cleanText(value)
                        .replace(",", "")
                        .replace("₹", "")
                        .replace("$", "")
                        .replace("€", "")
                        .replace("£", "")
                        .replace("¥", "")
                        .replace(" ", "");

        boolean negative =
                cleaned.startsWith("(")
                        && cleaned.endsWith(")");

        if (negative) {

            cleaned =
                    cleaned.substring(
                            1,
                            cleaned.length() - 1
                    );
        }

        if (cleaned.endsWith("%")) {

            cleaned =
                    cleaned.substring(
                            0,
                            cleaned.length() - 1
                    );

            double result =
                    Double.parseDouble(
                            cleaned
                    ) / 100.0;

            return negative
                    ? -result
                    : result;
        }

        double result =
                Double.parseDouble(
                        cleaned
                );

        if (!Double.isFinite(result)) {

            throw new IllegalArgumentException(
                    "Invalid numeric value: "
                            + value
            );
        }

        return negative
                ? -result
                : result;
    }

    private boolean isMissingValue(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            return true;
        }

        String normalized =
                value.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return normalized.equals("n/a")
                || normalized.equals("na")
                || normalized.equals("null")
                || normalized.equals("none")
                || normalized.equals("-")
                || normalized.equals("nan");
    }

    // =========================================================
    // AGGREGATION
    // =========================================================

    private double sumSalesRevenue(
            List<SalesRecord> records
    ) {

        return records.stream()
                .mapToDouble(
                        SalesRecord::getRevenue
                )
                .filter(Double::isFinite)
                .sum();
    }

    private double sumSalesQuantity(
            List<SalesRecord> records
    ) {

        return records.stream()
                .mapToDouble(
                        SalesRecord::getQuantity
                )
                .filter(Double::isFinite)
                .sum();
    }

    private double sumInventoryStock(
            List<InventoryRecord> records
    ) {

        return records.stream()
                .mapToDouble(
                        InventoryRecord::getStockAvailable
                )
                .filter(Double::isFinite)
                .sum();
    }

    private double sumStockoutHours(
            List<InventoryRecord> records
    ) {

        return records.stream()
                .mapToDouble(
                        InventoryRecord::getStockoutHours
                )
                .filter(Double::isFinite)
                .sum();
    }

    private double sumMarketingSpend(
            List<MarketingRecord> records
    ) {

        return records.stream()
                .mapToDouble(
                        MarketingRecord::getSpend
                )
                .filter(Double::isFinite)
                .sum();
    }

    private double sumMarketingConversions(
            List<MarketingRecord> records
    ) {

        /*
         * CRITICAL FIX:
         *
         * Every validated MarketingRecord contributes its
         * conversions value exactly once.
         *
         * No date filtering or campaign grouping is applied
         * to TOTAL marketing conversions.
         */
        double total =
                records.stream()
                        .mapToDouble(
                                MarketingRecord::getConversions
                        )
                        .filter(Double::isFinite)
                        .sum();

        return safeNumber(total);
    }

    private double averageSellingPrice(
            List<SalesRecord> records
    ) {

        if (records.isEmpty()) {
            return 0.0;
        }

        double quantity =
                sumSalesQuantity(
                        records
                );

        if (quantity == 0.0) {

            return records.stream()
                    .mapToDouble(
                            SalesRecord::getUnitPrice
                    )
                    .filter(Double::isFinite)
                    .average()
                    .orElse(0.0);
        }

        double weightedRevenue =
                records.stream()
                        .mapToDouble(
                                r ->
                                        safeNumber(
                                                r.getQuantity()
                                                        * r.getUnitPrice()
                                        )
                        )
                        .filter(Double::isFinite)
                        .sum();

        return weightedRevenue / quantity;
    }

    private double percentageChange(
            double baseline,
            double recent
    ) {

        if (
                !Double.isFinite(baseline)
                        || !Double.isFinite(recent)
        ) {
            return 0.0;
        }

        if (baseline == 0.0) {

            if (recent == 0.0) {
                return 0.0;
            }

            /*
             * Percentage change from zero is mathematically
             * undefined.
             *
             * We return a bounded directional signal rather
             * than an infinite value.
             */
            return recent > 0.0
                    ? 100.0
                    : -100.0;
        }

        double change =
                (
                        (recent - baseline)
                                / Math.abs(baseline)
                ) * 100.0;

        return Double.isFinite(change)
                ? change
                : 0.0;
    }

    // =========================================================
    // INSUFFICIENT HISTORY
    // =========================================================

    private List<String> buildInsufficientHistoryEvidence(
            List<SalesRecord> sales,
            List<InventoryRecord> inventory,
            List<MarketingRecord> marketing,
            Set<LocalDate> commonDates,
            SparseHistoryAssessment sparse
    ) {

        List<String> evidence =
                new ArrayList<>();

        evidence.add(
                "Common-date aligned observations: "
                        + commonDates.size()
        );

        evidence.add(
                "Sales records: "
                        + sales.size()
        );

        evidence.add(
                "Inventory records: "
                        + inventory.size()
        );

        evidence.add(
                "Marketing records: "
                        + marketing.size()
        );

        evidence.add(
                "Marketing conversions available: "
                        + round(
                        sumMarketingConversions(
                                marketing
                        )
                )
        );

        if (!sparse.reasons.isEmpty()) {

            evidence.add(
                    "Data sufficiency issues: "
                            + String.join(
                            "; ",
                            sparse.reasons
                    )
            );
        }

        return evidence;
    }

    private String buildClarificationRequest(
            SparseHistoryAssessment sparse
    ) {

        if (sparse.reasons.isEmpty()) {

            return "Please provide more aligned historical data before making a business decision.";
        }

        return "Clarification/data request: "
                + String.join(
                "; ",
                sparse.reasons
        )
                + ". Please provide additional aligned history or confirm the KPI definition before taking automated action.";
    }

    // =========================================================
    // ANALYTICAL METHOD
    // =========================================================

    private String buildAnalyticalMethod() {

        return "Semantic column detection + deterministic source validation + CSV/XLSX parsing + source-aware schema mapping + common-date alignment + observation-based baseline/recent split + sparse-history detection + deterministic KPI aggregation + cross-source movement analysis + movement-based driver prioritisation + traceable evidence + lineage-style source coverage + rule-based confidence scoring + confidence gating + abstention when evidence is insufficient. Quantitative truth is produced by deterministic analytical logic; the LLM is not used as the quantitative truth source.";
    }

    // =========================================================
    // SAFE HELPERS
    // =========================================================

    private String cleanText(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\uFEFF", "")
                .replace("\u00A0", " ")
                .trim()
                .replaceAll(
                        "^\"|\"$",
                        ""
                )
                .trim();
    }

    private String safeText(
            String value,
            String fallback
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {

            return fallback;
        }

        return value.trim();
    }

    private double safeNumber(
            double value
    ) {

        return Double.isFinite(value)
                ? value
                : 0.0;
    }

    private double round(
            double value
    ) {

        if (!Double.isFinite(value)) {
            return 0.0;
        }

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    private double clamp(
            double value,
            double min,
            double max
    ) {

        if (!Double.isFinite(value)) {
            return min;
        }

        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }
}

