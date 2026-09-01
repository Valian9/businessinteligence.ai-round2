package com.decisionlense.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
public class MultiSourceAnalysisService {

    // =========================================================
    // SALES RECORD
    // =========================================================

    public static class SalesRecord {

        private String date;
        private String productId;
        private String category;
        private String region;

        private double quantity;
        private double unitPrice;
        private double cost;
        private double revenue;

        public SalesRecord() {
        }

        public SalesRecord(
                String date,
                String productId,
                String category,
                String region,
                double quantity,
                double unitPrice,
                double cost,
                double revenue
        ) {
            this.date = date;
            this.productId = productId;
            this.category = category;
            this.region = region;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.cost = cost;
            this.revenue = revenue;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public double getCost() {
            return cost;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public double getRevenue() {
            return revenue;
        }

        public void setRevenue(double revenue) {
            this.revenue = revenue;
        }
    }

    // =========================================================
    // INVENTORY RECORD
    // =========================================================

    public static class InventoryRecord {

        private String date;
        private String productId;
        private String category;

        private double stockAvailable;
        private double stockoutHours;
        private double supplierDelay;

        public InventoryRecord() {
        }

        public InventoryRecord(
                String date,
                String productId,
                String category,
                double stockAvailable,
                double stockoutHours,
                double supplierDelay
        ) {
            this.date = date;
            this.productId = productId;
            this.category = category;
            this.stockAvailable = stockAvailable;
            this.stockoutHours = stockoutHours;
            this.supplierDelay = supplierDelay;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public double getStockAvailable() {
            return stockAvailable;
        }

        public void setStockAvailable(double stockAvailable) {
            this.stockAvailable = stockAvailable;
        }

        public double getStockoutHours() {
            return stockoutHours;
        }

        public void setStockoutHours(double stockoutHours) {
            this.stockoutHours = stockoutHours;
        }

        public double getSupplierDelay() {
            return supplierDelay;
        }

        public void setSupplierDelay(double supplierDelay) {
            this.supplierDelay = supplierDelay;
        }
    }

    // =========================================================
    // MARKETING RECORD
    // =========================================================

    public static class MarketingRecord {

        private String date;
        private String campaign;
        private String category;

        private double spend;
        private double impressions;
        private double clicks;
        private double conversions;

        public MarketingRecord() {
        }

        public MarketingRecord(
                String date,
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

    // =========================================================
    // RESPONSE
    // =========================================================

    public static class MultiSourceAnalysisResponse {

        private double totalRevenue;
        private double totalQuantity;
        private double totalStock;
        private double totalStockoutHours;
        private double totalMarketingSpend;
        private double totalConversions;

        private String kpi;
        private String movement;
        private double movementPercentage;

        // Driver movement signals
        private Map<String, Double> driverContributions;

        // Normalized impact scores
        private Map<String, Double> driverImpactScores;

        // High / Medium / Low
        private Map<String, String> driverImpactLevels;

        private String primaryDriver;
        private List<String> evidence;

        private String driverExplanation;
        private double confidence;

        private String analyticalMethod;
        private String recommendation;

        private String owner;
        private String monitoringPlan;

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

        public void setTotalStockoutHours(double totalStockoutHours) {
            this.totalStockoutHours = totalStockoutHours;
        }

        public double getTotalMarketingSpend() {
            return totalMarketingSpend;
        }

        public void setTotalMarketingSpend(double totalMarketingSpend) {
            this.totalMarketingSpend = totalMarketingSpend;
        }

        public double getTotalConversions() {
            return totalConversions;
        }

        public void setTotalConversions(double totalConversions) {
            this.totalConversions = totalConversions;
        }

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

        public Map<String, Double> getDriverContributions() {
            return driverContributions;
        }

        public void setDriverContributions(
                Map<String, Double> driverContributions
        ) {
            this.driverContributions = driverContributions;
        }

        public Map<String, Double> getDriverImpactScores() {
            return driverImpactScores;
        }

        public void setDriverImpactScores(
                Map<String, Double> driverImpactScores
        ) {
            this.driverImpactScores = driverImpactScores;
        }

        public Map<String, String> getDriverImpactLevels() {
            return driverImpactLevels;
        }

        public void setDriverImpactLevels(
                Map<String, String> driverImpactLevels
        ) {
            this.driverImpactLevels = driverImpactLevels;
        }

        public String getPrimaryDriver() {
            return primaryDriver;
        }

        public void setPrimaryDriver(String primaryDriver) {
            this.primaryDriver = primaryDriver;
        }

        public List<String> getEvidence() {
            return evidence;
        }

        public void setEvidence(List<String> evidence) {
            this.evidence = evidence;
        }

        public String getDriverExplanation() {
            return driverExplanation;
        }

        public void setDriverExplanation(String driverExplanation) {
            this.driverExplanation = driverExplanation;
        }

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
    }

    // =========================================================
    // MAIN ENTRY
    // =========================================================

    public MultiSourceAnalysisResponse analyze(
            MultipartFile salesFile,
            MultipartFile inventoryFile,
            MultipartFile marketingFile
    ) throws IOException {

        if (salesFile == null || salesFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sales file is required."
            );
        }

        if (inventoryFile == null || inventoryFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "Inventory file is required."
            );
        }

        if (marketingFile == null || marketingFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "Marketing file is required."
            );
        }

        List<SalesRecord> salesRecords =
                readSalesFile(salesFile);

        List<InventoryRecord> inventoryRecords =
                readInventoryFile(inventoryFile);

        List<MarketingRecord> marketingRecords =
                readMarketingFile(marketingFile);

        if (salesRecords.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sales file contains no valid records."
            );
        }

        if (inventoryRecords.isEmpty()) {
            throw new IllegalArgumentException(
                    "Inventory file contains no valid records."
            );
        }

        if (marketingRecords.isEmpty()) {
            throw new IllegalArgumentException(
                    "Marketing file contains no valid records."
            );
        }

        return buildAnalysis(
                salesRecords,
                inventoryRecords,
                marketingRecords
        );
    }

    // =========================================================
    // MAIN ANALYSIS
    // =========================================================

    private MultiSourceAnalysisResponse buildAnalysis(
            List<SalesRecord> sales,
            List<InventoryRecord> inventory,
            List<MarketingRecord> marketing
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

        double totalStock =
                sumInventoryStock(inventory);

        double totalStockoutHours =
                sumInventoryStockout(inventory);

        double totalMarketingSpend =
                sumMarketingSpend(marketing);

        double totalConversions =
                sumMarketingConversions(marketing);

        response.setTotalRevenue(
                round(totalRevenue)
        );

        response.setTotalQuantity(
                round(totalQuantity)
        );

        response.setTotalStock(
                round(totalStock)
        );

        response.setTotalStockoutHours(
                round(totalStockoutHours)
        );

        response.setTotalMarketingSpend(
                round(totalMarketingSpend)
        );

        response.setTotalConversions(
                round(totalConversions)
        );

        // =====================================================
        // COMMON DATE RANGE
        // =====================================================

        Set<LocalDate> allDates =
                new TreeSet<>();

        addSalesDates(
                allDates,
                sales
        );

        addInventoryDates(
                allDates,
                inventory
        );

        addMarketingDates(
                allDates,
                marketing
        );

        if (allDates.size() < 2) {

            throw new IllegalArgumentException(
                    "At least two distinct dates across Sales, Inventory and Marketing are required."
            );
        }

        List<LocalDate> sortedDates =
                new ArrayList<>(allDates);

        LocalDate firstDate =
                sortedDates.get(0);

        LocalDate lastDate =
                sortedDates.get(
                        sortedDates.size() - 1
                );

        // =====================================================
        // PERIOD SPLIT
        // =====================================================

        int splitIndex =
                sortedDates.size() / 2;

        LocalDate recentStartDate =
                sortedDates.get(splitIndex);

        LocalDate baselineEndDate =
                recentStartDate.minusDays(1);

        // =====================================================
        // SALES PERIODS
        // =====================================================

        List<SalesRecord> baselineSales =
                sales.stream()
                        .filter(record ->
                                isBefore(
                                        record.getDate(),
                                        recentStartDate
                                )
                        )
                        .toList();

        List<SalesRecord> recentSales =
                sales.stream()
                        .filter(record ->
                                isOnOrAfter(
                                        record.getDate(),
                                        recentStartDate
                                )
                        )
                        .toList();

        // =====================================================
        // INVENTORY PERIODS
        // =====================================================

        List<InventoryRecord> baselineInventory =
                inventory.stream()
                        .filter(record ->
                                isBefore(
                                        record.getDate(),
                                        recentStartDate
                                )
                        )
                        .toList();

        List<InventoryRecord> recentInventory =
                inventory.stream()
                        .filter(record ->
                                isOnOrAfter(
                                        record.getDate(),
                                        recentStartDate
                                )
                        )
                        .toList();

        // =====================================================
        // MARKETING PERIODS
        // =====================================================

        List<MarketingRecord> baselineMarketing =
                marketing.stream()
                        .filter(record ->
                                isBefore(
                                        record.getDate(),
                                        recentStartDate
                                )
                        )
                        .toList();

        List<MarketingRecord> recentMarketing =
                marketing.stream()
                        .filter(record ->
                                isOnOrAfter(
                                        record.getDate(),
                                        recentStartDate
                                )
                        )
                        .toList();

        // =====================================================
        // SAFETY CHECK
        // =====================================================

        if (
                baselineSales.isEmpty()
                        || recentSales.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Sales data must contain records in both baseline and recent periods."
            );
        }

        // =====================================================
        // PERIOD KPI VALUES
        // =====================================================

        double baselineRevenue =
                sumSalesRevenue(
                        baselineSales
                );

        double recentRevenue =
                sumSalesRevenue(
                        recentSales
                );

        double baselineQuantity =
                sumSalesQuantity(
                        baselineSales
                );

        double recentQuantity =
                sumSalesQuantity(
                        recentSales
                );

        double baselineAveragePrice =
                baselineQuantity == 0
                        ? 0
                        : baselineRevenue / baselineQuantity;

        double recentAveragePrice =
                recentQuantity == 0
                        ? 0
                        : recentRevenue / recentQuantity;

        double baselineStock =
                sumInventoryStock(
                        baselineInventory
                );

        double recentStock =
                sumInventoryStock(
                        recentInventory
                );

        double baselineStockout =
                sumInventoryStockout(
                        baselineInventory
                );

        double recentStockout =
                sumInventoryStockout(
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

        // =====================================================
        // KPI MOVEMENTS
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

        double priceChange =
                percentageChange(
                        baselineAveragePrice,
                        recentAveragePrice
                );

        double stockChange =
                percentageChange(
                        baselineStock,
                        recentStock
                );

        double stockoutDelta =
                recentStockout -
                        baselineStockout;

        double conversionChange =
                percentageChange(
                        baselineConversions,
                        recentConversions
                );

        // =====================================================
        // KPI RESPONSE
        // =====================================================

        response.setKpi(
                "Revenue"
        );

        response.setMovement(
                revenueChange < 0
                        ? "↓ Revenue"
                        : "↑ Revenue"
        );

        response.setMovementPercentage(
                round(revenueChange)
        );

        // =====================================================
        // REVENUE DECOMPOSITION
        // =====================================================

        double volumeEffect =
                (
                        recentQuantity
                                - baselineQuantity
                )
                        * baselineAveragePrice;

        double priceEffect =
                recentQuantity
                        * (
                        recentAveragePrice
                                - baselineAveragePrice
                );

        double volumeContribution =
                baselineRevenue == 0
                        ? 0
                        : (
                        volumeEffect
                                / baselineRevenue
                ) * 100.0;

        double priceContribution =
                baselineRevenue == 0
                        ? 0
                        : (
                        priceEffect
                                / baselineRevenue
                ) * 100.0;

        // =====================================================
        // DRIVER MOVEMENT + IMPACT SCORING
        // =====================================================

        Map<String, Double> drivers =
                new LinkedHashMap<>();

        Map<String, Double> impactScores =
                new LinkedHashMap<>();

        Map<String, String> impactLevels =
                new LinkedHashMap<>();

        // -----------------------------------------------------
        // Driver movement signals
        // -----------------------------------------------------

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
                round(stockChange)
        );

        drivers.put(
                "Stockout Hours",
                round(stockoutDelta)
        );

        drivers.put(
                "Marketing Conversions",
                round(conversionChange)
        );

        // -----------------------------------------------------
        // Impact scores
        // -----------------------------------------------------

        double salesVolumeImpact =
                calculateImpactScore(
                        quantityChange,
                        false
                );

        double priceImpact =
                calculateImpactScore(
                        priceChange,
                        false
                );

        double inventoryImpact =
                calculateImpactScore(
                        stockChange,
                        false
                );

        double stockoutImpact =
                calculateImpactScore(
                        stockoutDelta,
                        true
                );

        double marketingImpact =
                calculateImpactScore(
                        conversionChange,
                        false
                );

        impactScores.put(
                "Sales Volume",
                salesVolumeImpact
        );

        impactScores.put(
                "Average Price",
                priceImpact
        );

        impactScores.put(
                "Inventory Availability",
                inventoryImpact
        );

        impactScores.put(
                "Stockout Hours",
                stockoutImpact
        );

        impactScores.put(
                "Marketing Conversions",
                marketingImpact
        );

        // -----------------------------------------------------
        // Impact levels
        // -----------------------------------------------------

        for (
                Map.Entry<String, Double> entry :
                impactScores.entrySet()
        ) {

            impactLevels.put(
                    entry.getKey(),
                    getImpactLevel(
                            entry.getValue()
                    )
            );
        }

        response.setDriverContributions(
                drivers
        );

        response.setDriverImpactScores(
                impactScores
        );

        response.setDriverImpactLevels(
                impactLevels
        );

        // =====================================================
        // PRIMARY DRIVER
        // =====================================================

        String primaryDriver;

        if (
                quantityChange <= -20
                        && stockChange <= -15
                        && stockoutDelta > 0
        ) {

            primaryDriver =
                    "Sales Volume constrained by Inventory Availability";

        } else if (
                quantityChange <= -15
                        && conversionChange <= -20
        ) {

            primaryDriver =
                    "Sales Volume + Marketing Conversion Decline";

        } else if (
                stockChange <= -15
                        && stockoutDelta > 0
        ) {

            primaryDriver =
                    "Inventory Availability + Stockout Risk";

        } else if (
                quantityChange <= -10
        ) {

            primaryDriver =
                    "Sales Volume";

        } else if (
                priceChange <= -10
        ) {

            primaryDriver =
                    "Average Price";

        } else if (
                conversionChange <= -15
        ) {

            primaryDriver =
                    "Marketing Conversions";

        } else {

            primaryDriver =
                    "Multiple interacting drivers";
        }

        response.setPrimaryDriver(
                primaryDriver
        );

        // =====================================================
        // TRACEABLE EXPLANATION
        // =====================================================

        StringBuilder explanation =
                new StringBuilder();

        explanation.append(
                "Revenue changed from ₹"
                        + formatNumber(
                        baselineRevenue
                )
                        + " to ₹"
                        + formatNumber(
                        recentRevenue
                )
                        + " ("
                        + formatPercent(
                        revenueChange
                )
                        + "). "
        );

        explanation.append(
                "Sales quantity changed from "
                        + formatNumber(
                        baselineQuantity
                )
                        + " to "
                        + formatNumber(
                        recentQuantity
                )
                        + " ("
                        + formatPercent(
                        quantityChange
                )
                        + "). "
        );

        explanation.append(
                "Average selling price changed from ₹"
                        + formatNumber(
                        baselineAveragePrice
                )
                        + " to ₹"
                        + formatNumber(
                        recentAveragePrice
                )
                        + " ("
                        + formatPercent(
                        priceChange
                )
                        + "). "
        );

        if (stockChange < 0) {

            explanation.append(
                    "Inventory availability declined by "
                            + formatPercent(
                            stockChange
                    )
                            + ". "
            );
        }

        if (stockoutDelta > 0) {

            explanation.append(
                    "Stockout exposure increased by "
                            + formatNumber(
                            stockoutDelta
                    )
                            + " hours. "
            );
        }

        if (conversionChange < 0) {

            explanation.append(
                    "Marketing conversions declined by "
                            + formatPercent(
                            conversionChange
                    )
                            + ". "
            );
        }

        response.setDriverExplanation(
                explanation.toString().trim()
        );

        // =====================================================
        // EVIDENCE
        // =====================================================

        List<String> evidence =
                new ArrayList<>();

        evidence.add(
                "Baseline period: "
                        + firstDate
                        + " to "
                        + baselineEndDate
                        + "."
        );

        evidence.add(
                "Recent period: "
                        + recentStartDate
                        + " to "
                        + lastDate
                        + "."
        );

        evidence.add(
                "Revenue changed from ₹"
                        + formatNumber(
                        baselineRevenue
                )
                        + " to ₹"
                        + formatNumber(
                        recentRevenue
                )
                        + " ("
                        + formatPercent(
                        revenueChange
                )
                        + ")."
        );

        evidence.add(
                "Quantity changed from "
                        + formatNumber(
                        baselineQuantity
                )
                        + " to "
                        + formatNumber(
                        recentQuantity
                )
                        + " ("
                        + formatPercent(
                        quantityChange
                )
                        + ")."
        );

        evidence.add(
                "Average price changed from ₹"
                        + formatNumber(
                        baselineAveragePrice
                )
                        + " to ₹"
                        + formatNumber(
                        recentAveragePrice
                )
                        + " ("
                        + formatPercent(
                        priceChange
                )
                        + ")."
        );

        evidence.add(
                "Inventory changed from "
                        + formatNumber(
                        baselineStock
                )
                        + " to "
                        + formatNumber(
                        recentStock
                )
                        + " ("
                        + formatPercent(
                        stockChange
                )
                        + ")."
        );

        evidence.add(
                "Stockout exposure changed from "
                        + formatNumber(
                        baselineStockout
                )
                        + " to "
                        + formatNumber(
                        recentStockout
                )
                        + " hours."
        );

        evidence.add(
                "Marketing conversions changed from "
                        + formatNumber(
                        baselineConversions
                )
                        + " to "
                        + formatNumber(
                        recentConversions
                )
                        + " ("
                        + formatPercent(
                        conversionChange
                )
                        + ")."
        );

        // =====================================================
        // PRODUCT EVIDENCE
        // =====================================================

        addTopProductEvidence(
                evidence,
                baselineSales,
                recentSales
        );

        // =====================================================
        // INVENTORY EVIDENCE
        // =====================================================

        addInventoryEvidence(
                evidence,
                recentInventory
        );

        // =====================================================
        // MARKETING EVIDENCE
        // =====================================================

        addMarketingEvidence(
                evidence,
                recentMarketing
        );

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
                        quantityChange,
                        stockChange,
                        stockoutDelta,
                        conversionChange
                );

        response.setConfidence(
                round(confidence)
        );

        // =====================================================
        // METHOD
        // =====================================================

        response.setAnalyticalMethod(
                "Common-date-aligned deterministic revenue "
                        + "decomposition + cross-source movement "
                        + "analysis + impact scoring + traceable evidence"
        );

        // =====================================================
        // RECOMMENDATION
        // =====================================================

        response.setRecommendation(
                buildRecommendation(
                        primaryDriver
                )
        );

        // =====================================================
        // OWNER
        // =====================================================

        response.setOwner(
                determineOwner(
                        primaryDriver
                )
        );

        // =====================================================
        // MONITORING
        // =====================================================

        response.setMonitoringPlan(
                buildMonitoringPlan(
                        primaryDriver
                )
        );

        return response;
    }

    // =========================================================
    // DATE COLLECTION
    // =========================================================

    private void addSalesDates(
            Set<LocalDate> dates,
            List<SalesRecord> records
    ) {

        for (SalesRecord record : records) {

            if (
                    record.getDate() != null
                            && !record.getDate().isBlank()
            ) {

                dates.add(
                        parseDate(
                                record.getDate()
                        )
                );
            }
        }
    }

    private void addInventoryDates(
            Set<LocalDate> dates,
            List<InventoryRecord> records
    ) {

        for (InventoryRecord record : records) {

            if (
                    record.getDate() != null
                            && !record.getDate().isBlank()
            ) {

                dates.add(
                        parseDate(
                                record.getDate()
                        )
                );
            }
        }
    }

    private void addMarketingDates(
            Set<LocalDate> dates,
            List<MarketingRecord> records
    ) {

        for (MarketingRecord record : records) {

            if (
                    record.getDate() != null
                            && !record.getDate().isBlank()
            ) {

                dates.add(
                        parseDate(
                                record.getDate()
                        )
                );
            }
        }
    }

    private boolean isBefore(
            String date,
            LocalDate comparisonDate
    ) {

        return parseDate(date)
                .isBefore(comparisonDate);
    }

    private boolean isOnOrAfter(
            String date,
            LocalDate comparisonDate
    ) {

        return !parseDate(date)
                .isBefore(comparisonDate);
    }

    // =========================================================
    // RECOMMENDATION
    // =========================================================

    private String buildRecommendation(
            String primaryDriver
    ) {

        if (
                primaryDriver.contains(
                        "Inventory"
                )
        ) {

            return
                    "Prioritize replenishment of constrained "
                            + "products, investigate supplier delays, "
                            + "reduce stockout exposure and monitor "
                            + "availability for revenue-critical products.";
        }

        if (
                primaryDriver.equals(
                        "Sales Volume"
                )
        ) {

            return
                    "Investigate products and regions with the "
                            + "largest quantity decline and prioritize "
                            + "recovery actions for high-revenue items.";
        }

        if (
                primaryDriver.equals(
                        "Average Price"
                )
        ) {

            return
                    "Review pricing, discounting and product mix "
                            + "to determine whether price changes are "
                            + "affecting revenue performance.";
        }

        if (
                primaryDriver.equals(
                        "Marketing Conversions"
                )
        ) {

            return
                    "Review campaign efficiency and reallocate "
                            + "spend toward campaigns and categories "
                            + "with stronger conversion performance.";
        }

        if (
                primaryDriver.contains(
                        "Marketing"
                )
        ) {

            return
                    "Coordinate sales and marketing teams to "
                            + "investigate the conversion decline while "
                            + "protecting high-performing campaigns.";
        }

        return
                "Run a focused cross-functional review across "
                        + "sales, inventory and marketing because "
                        + "multiple interacting signals are present.";
    }

    // =========================================================
    // OWNER
    // =========================================================

    private String determineOwner(
            String primaryDriver
    ) {

        if (
                primaryDriver.contains(
                        "Inventory"
                )
        ) {

            return "Supply Chain Manager";
        }

        if (
                primaryDriver.contains(
                        "Sales"
                )
        ) {

            return "Sales Manager";
        }

        if (
                primaryDriver.contains(
                        "Price"
                )
        ) {

            return "Pricing / Commercial Manager";
        }

        if (
                primaryDriver.contains(
                        "Marketing"
                )
        ) {

            return "Marketing Manager";
        }

        return "Business Operations Manager";
    }

    // =========================================================
    // MONITORING
    // =========================================================

    private String buildMonitoringPlan(
            String primaryDriver
    ) {

        if (
                primaryDriver.contains(
                        "Inventory"
                )
        ) {

            return
                    "Monitor stock availability, stockout hours "
                            + "and supplier delay daily for affected products.";
        }

        if (
                primaryDriver.contains(
                        "Sales"
                )
        ) {

            return
                    "Monitor daily quantity, revenue by product "
                            + "and region, and recovery of high-impact products.";
        }

        if (
                primaryDriver.contains(
                        "Price"
                )
        ) {

            return
                    "Monitor average selling price, discount rate "
                            + "and revenue per unit.";
        }

        if (
                primaryDriver.contains(
                        "Marketing"
                )
        ) {

            return
                    "Monitor marketing spend, clicks, conversion "
                            + "rate and campaign efficiency.";
        }

        return
                "Monitor revenue, quantity, inventory and "
                        + "marketing KPIs together for the next reporting cycle.";
    }

    // =========================================================
    // PRODUCT EVIDENCE
    // =========================================================

    private void addTopProductEvidence(
            List<String> evidence,
            List<SalesRecord> baselineSales,
            List<SalesRecord> recentSales
    ) {

        Map<String, Double> baselineByProduct =
                new HashMap<>();

        Map<String, Double> recentByProduct =
                new HashMap<>();

        for (
                SalesRecord record :
                baselineSales
        ) {

            String product =
                    safeString(
                            record.getProductId(),
                            "Unknown"
                    );

            baselineByProduct.merge(
                    product,
                    record.getRevenue(),
                    Double::sum
            );
        }

        for (
                SalesRecord record :
                recentSales
        ) {

            String product =
                    safeString(
                            record.getProductId(),
                            "Unknown"
                    );

            recentByProduct.merge(
                    product,
                    record.getRevenue(),
                    Double::sum
            );
        }

        String worstProduct = null;

        double worstChange = 0;

        for (
                String product :
                baselineByProduct.keySet()
        ) {

            double baseline =
                    baselineByProduct.getOrDefault(
                            product,
                            0.0
                    );

            double recent =
                    recentByProduct.getOrDefault(
                            product,
                            0.0
                    );

            double change =
                    percentageChange(
                            baseline,
                            recent
                    );

            if (
                    change < worstChange
            ) {

                worstChange =
                        change;

                worstProduct =
                        product;
            }
        }

        if (
                worstProduct != null
        ) {

            evidence.add(
                    "Product "
                            + worstProduct
                            + " had the largest "
                            + "revenue decline at "
                            + formatPercent(
                            worstChange
                    )
                            + "."
            );
        }
    }

    // =========================================================
    // INVENTORY EVIDENCE
    // =========================================================

    private void addInventoryEvidence(
            List<String> evidence,
            List<InventoryRecord> inventory
    ) {

        if (inventory.isEmpty()) {
            return;
        }

        InventoryRecord highestStockout =
                inventory.stream()
                        .max(
                                Comparator.comparingDouble(
                                        InventoryRecord::getStockoutHours
                                )
                        )
                        .orElse(null);

        if (
                highestStockout != null
                        && highestStockout
                        .getStockoutHours() > 0
        ) {

            evidence.add(
                    "Highest recorded stockout exposure "
                            + "was "
                            + formatNumber(
                            highestStockout
                                    .getStockoutHours()
                    )
                            + " hours for product "
                            + safeString(
                            highestStockout.getProductId(),
                            "Unknown"
                    )
                            + "."
            );
        }

        InventoryRecord longestDelay =
                inventory.stream()
                        .max(
                                Comparator.comparingDouble(
                                        InventoryRecord::getSupplierDelay
                                )
                        )
                        .orElse(null);

        if (
                longestDelay != null
                        && longestDelay
                        .getSupplierDelay() > 0
        ) {

            evidence.add(
                    "Maximum supplier delay reached "
                            + formatNumber(
                            longestDelay
                                    .getSupplierDelay()
                    )
                            + " days for product "
                            + safeString(
                            longestDelay.getProductId(),
                            "Unknown"
                    )
                            + "."
            );
        }
    }

    // =========================================================
    // MARKETING EVIDENCE
    // =========================================================

    private void addMarketingEvidence(
            List<String> evidence,
            List<MarketingRecord> marketing
    ) {

        if (marketing.isEmpty()) {
            return;
        }

        MarketingRecord best =
                marketing.stream()
                        .max(
                                Comparator.comparingDouble(
                                        MarketingRecord::getConversions
                                )
                        )
                        .orElse(null);

        if (
                best != null
        ) {

            evidence.add(
                    "Highest recorded marketing "
                            + "conversions were "
                            + formatNumber(
                            best.getConversions()
                    )
                            + " under campaign "
                            + safeString(
                            best.getCampaign(),
                            "Unknown"
                    )
                            + "."
            );
        }

        double totalSpend =
                sumMarketingSpend(
                        marketing
                );

        double totalConversion =
                sumMarketingConversions(
                        marketing
                );

        if (
                totalSpend > 0
        ) {

            double conversionEfficiency =
                    totalConversion /
                            totalSpend;

            evidence.add(
                    "Recent marketing conversion "
                            + "efficiency was "
                            + String.format(
                            Locale.US,
                            "%.4f conversions per currency unit spent.",
                            conversionEfficiency
                    )
            );
        }
    }

    // =========================================================
    // CONFIDENCE
    // =========================================================

    private double calculateConfidence(
            List<SalesRecord> sales,
            List<InventoryRecord> inventory,
            List<MarketingRecord> marketing,
            double quantityChange,
            double stockChange,
            double stockoutDelta,
            double conversionChange
    ) {

        double confidence =
                0.60;

        if (
                sales.size() >= 10
        ) {

            confidence += 0.08;
        }

        if (
                inventory.size() >= 10
        ) {

            confidence += 0.08;
        }

        if (
                marketing.size() >= 10
        ) {

            confidence += 0.08;
        }

        boolean supplySignal =
                stockChange < -10
                        && stockoutDelta > 0;

        boolean salesSignal =
                quantityChange < -5;

        boolean marketingSignal =
                conversionChange < -5;

        if (
                supplySignal
                        && salesSignal
        ) {

            confidence += 0.08;
        }

        if (
                marketingSignal
                        && salesSignal
        ) {

            confidence += 0.05;
        }

        return Math.min(
                confidence,
                0.95
        );
    }

    // =========================================================
    // SALES CSV READER
    // =========================================================

    private List<SalesRecord> readSalesFile(
            MultipartFile file
    ) throws IOException {

        List<SalesRecord> records =
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

            String header =
                    reader.readLine();

            if (
                    header == null
                            || header.trim().isEmpty()
            ) {

                return records;
            }

            Map<String, Integer> columns =
                    buildColumnMap(
                            header
                    );

            validateRequiredColumns(
                    columns,
                    List.of(
                            "date",
                            "product_id",
                            "quantity",
                            "revenue"
                    ),
                    "Sales"
            );

            String line;

            while (
                    (line =
                            reader.readLine()) != null
            ) {

                if (
                        line.trim().isEmpty()
                ) {

                    continue;
                }

                String[] values =
                        splitCsv(
                                line
                        );

                String date =
                        getValue(
                                values,
                                columns,
                                "date"
                        );

                if (
                        date.isBlank()
                ) {
                    continue;
                }

                String productId =
                        getValue(
                                values,
                                columns,
                                "product_id"
                        );

                String category =
                        getValue(
                                values,
                                columns,
                                "category"
                        );

                String region =
                        getValue(
                                values,
                                columns,
                                "region"
                        );

                double quantity =
                        parseDouble(
                                getValue(
                                        values,
                                        columns,
                                        "quantity"
                                )
                        );

                double unitPrice =
                        parseDouble(
                                getValue(
                                        values,
                                        columns,
                                        "unit_price"
                                )
                        );

                double cost =
                        parseDouble(
                                getValue(
                                        values,
                                        columns,
                                        "cost"
                                )
                        );

                double revenue =
                        parseDouble(
                                getValue(
                                        values,
                                        columns,
                                        "revenue"
                                )
                        );

                records.add(
                        new SalesRecord(
                                date,
                                productId,
                                category,
                                region,
                                quantity,
                                unitPrice,
                                cost,
                                revenue
                        )
                );
            }
        }

        return records;
    }

    // =========================================================
    // INVENTORY CSV READER
    // =========================================================

    private List<InventoryRecord> readInventoryFile(
            MultipartFile file
    ) throws IOException {

        List<InventoryRecord> records =
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

            String header =
                    reader.readLine();

            if (
                    header == null
                            || header.trim().isEmpty()
            ) {

                return records;
            }

            Map<String, Integer> columns =
                    buildColumnMap(
                            header
                    );

            validateRequiredColumns(
                    columns,
                    List.of(
                            "date",
                            "product_id",
                            "stock_available",
                            "stockout_hours",
                            "supplier_delay"
                    ),
                    "Inventory"
            );

            String line;

            while (
                    (line =
                            reader.readLine()) != null
            ) {

                if (
                        line.trim().isEmpty()
                ) {

                    continue;
                }

                String[] values =
                        splitCsv(
                                line
                        );

                String date =
                        getValue(
                                values,
                                columns,
                                "date"
                        );

                if (
                        date.isBlank()
                ) {
                    continue;
                }

                String productId =
                        getValue(
                                values,
                                columns,
                                "product_id"
                        );

                String category =
                        getValue(
                                values,
                                columns,
                                "category"
                        );

                double stockAvailable =
                        parseDouble(
                                getValue(
                                        values,
                                        columns,
                                        "stock_available"
                                )
                        );

                double stockoutHours =
                        parseDouble(
                                getValue(
                                        values,
                                        columns,
                                        "stockout_hours"
                                )
                        );

                double supplierDelay =
                        parseDouble(
                                getValue(
                                        values,
                                        columns,
                                        "supplier_delay"
                                )
                        );

                records.add(
                        new InventoryRecord(
                                date,
                                productId,
                                category,
                                stockAvailable,
                                stockoutHours,
                                supplierDelay
                        )
                );
            }
        }

        return records;
    }

    // =========================================================
    // MARKETING CSV READER
    // =========================================================

    private List<MarketingRecord> readMarketingFile(
            MultipartFile file
    ) throws IOException {

        List<MarketingRecord> records =
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

            String header =
                    reader.readLine();

            if (
                    header == null
                            || header.trim().isEmpty()
            ) {

                return records;
            }

            Map<String, Integer> columns =
                    buildColumnMap(
                            header
                    );

            validateRequiredColumns(
                    columns,
                    List.of(
                            "date",
                            "campaign",
                            "spend",
                            "conversions"
                    ),
                    "Marketing"
            );

            String line;

            while (
                    (line =
                            reader.readLine()) != null
            ) {

                if (
                        line.trim().isEmpty()
                ) {

                    continue;
                }

                String[] values =
                        splitCsv(
                                line
                        );

                String date =
                        getValue(
                                values,
                                columns,
                                "date"
                        );

                if (
                        date.isBlank()
                ) {
                    continue;
                }

                String campaign =
                        getValue(
                                values,
                                columns,
                                "campaign"
                        );

                String category =
                        getValue(
                                values,
                                columns,
                                "category"
                        );

                double spend =
                        parseDouble(
                                getValue(
                                        values,
                                        columns,
                                        "spend"
                                )
                        );

                double impressions =
                        parseDouble(
                                getValue(
                                        values,
                                        columns,
                                        "impressions"
                                )
                        );

                double clicks =
                        parseDouble(
                                getValue(
                                        values,
                                        columns,
                                        "clicks"
                                )
                        );

                double conversions =
                        parseDouble(
                                getValue(
                                        values,
                                        columns,
                                        "conversions"
                                )
                        );

                records.add(
                        new MarketingRecord(
                                date,
                                campaign,
                                category,
                                spend,
                                impressions,
                                clicks,
                                conversions
                        )
                );
            }
        }

        return records;
    }

    // =========================================================
    // CSV UTILITIES
    // =========================================================

    private Map<String, Integer> buildColumnMap(
            String header
    ) {

        Map<String, Integer> columns =
                new HashMap<>();

        String[] headers =
                splitCsv(
                        header
                );

        for (
                int i = 0;
                i < headers.length;
                i++
        ) {

            String column =
                    headers[i]
                            .trim()
                            .replace(
                                    "\uFEFF",
                                    ""
                            )
                            .replace(
                                    "\"",
                                    ""
                            )
                            .toLowerCase(
                                    Locale.ROOT
                            );

            columns.put(
                    column,
                    i
            );
        }

        return columns;
    }

    private void validateRequiredColumns(
            Map<String, Integer> columns,
            List<String> requiredColumns,
            String sourceName
    ) {

        List<String> missing =
                new ArrayList<>();

        for (
                String column :
                requiredColumns
        ) {

            if (
                    !columns.containsKey(column)
            ) {

                missing.add(column);
            }
        }

        if (
                !missing.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    sourceName
                            + " file is missing required column(s): "
                            + String.join(
                            ", ",
                            missing
                    )
            );
        }
    }

    private String getValue(
            String[] values,
            Map<String, Integer> columns,
            String column
    ) {

        Integer index =
                columns.get(
                        column
                );

        if (
                index == null
                        || index < 0
                        || index >= values.length
        ) {

            return "";
        }

        return values[index]
                .trim()
                .replace(
                        "\"",
                        ""
                );
    }

    private String[] splitCsv(
            String line
    ) {

        return line.split(
                ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",
                -1
        );
    }

    private double parseDouble(
            String value
    ) {

        if (
                value == null
                        || value.trim().isEmpty()
        ) {

            return 0;
        }

        try {

            return Double.parseDouble(
                    value.trim()
                            .replace(
                                    "\"",
                                    ""
                            )
                            .replace(
                                    ",",
                                    ""
                            )
            );

        } catch (
                NumberFormatException e
        ) {

            return 0;
        }
    }

    // =========================================================
    // DATE PARSER
    // =========================================================

    private LocalDate parseDate(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Invalid or missing date value."
            );
        }

        String date =
                value.trim()
                        .replace(
                                "\"",
                                ""
                        );

        List<DateTimeFormatter> formatters =
                List.of(

                        DateTimeFormatter.ISO_LOCAL_DATE,

                        DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy"
                        ),

                        DateTimeFormatter.ofPattern(
                                "d/M/yyyy"
                        ),

                        DateTimeFormatter.ofPattern(
                                "MM/dd/yyyy"
                        ),

                        DateTimeFormatter.ofPattern(
                                "M/d/yyyy"
                        ),

                        DateTimeFormatter.ofPattern(
                                "dd-MM-yyyy"
                        ),

                        DateTimeFormatter.ofPattern(
                                "d-M-yyyy"
                        )
                );

        for (
                DateTimeFormatter formatter :
                formatters
        ) {

            try {

                return LocalDate.parse(
                        date,
                        formatter
                );

            } catch (
                    DateTimeParseException ignored
            ) {

                // Try next format
            }
        }

        throw new IllegalArgumentException(
                "Unsupported date format: "
                        + value
                        + ". Use a standard date such as "
                        + "2026-08-01 or 01/08/2026."
        );
    }

    // =========================================================
    // SUM HELPERS
    // =========================================================

    private double sumSalesRevenue(
            List<SalesRecord> records
    ) {

        return records.stream()
                .mapToDouble(
                        SalesRecord::getRevenue
                )
                .sum();
    }

    private double sumSalesQuantity(
            List<SalesRecord> records
    ) {

        return records.stream()
                .mapToDouble(
                        SalesRecord::getQuantity
                )
                .sum();
    }

    private double sumInventoryStock(
            List<InventoryRecord> records
    ) {

        return records.stream()
                .mapToDouble(
                        InventoryRecord::getStockAvailable
                )
                .sum();
    }

    private double sumInventoryStockout(
            List<InventoryRecord> records
    ) {

        return records.stream()
                .mapToDouble(
                        InventoryRecord::getStockoutHours
                )
                .sum();
    }

    private double sumMarketingSpend(
            List<MarketingRecord> records
    ) {

        return records.stream()
                .mapToDouble(
                        MarketingRecord::getSpend
                )
                .sum();
    }

    private double sumMarketingConversions(
            List<MarketingRecord> records
    ) {

        return records.stream()
                .mapToDouble(
                        MarketingRecord::getConversions
                )
                .sum();
    }

    // =========================================================
    // MATH
    // =========================================================

    private double percentageChange(
            double oldValue,
            double newValue
    ) {

        if (
                oldValue == 0
        ) {

            if (
                    newValue == 0
            ) {

                return 0;
            }

            return 100;
        }

        return (
                (newValue - oldValue)
                        / oldValue
        ) * 100.0;
    }

    private double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    private String formatPercent(
            double value
    ) {

        return String.format(
                Locale.US,
                "%.1f%%",
                value
        );
    }

    private String formatNumber(
            double value
    ) {

        return String.format(
                Locale.US,
                "%,.2f",
                value
        );
    }

    // =========================================================
    // DRIVER IMPACT SCORE
    // =========================================================

    private double calculateImpactScore(
            double change,
            boolean positiveChangeIsBad
    ) {

        double magnitude =
                Math.abs(change);

        /*
         * Impact score represents the strength
         * of a business movement signal.
         *
         * It is a prioritization score,
         * NOT a causal probability.
         */

        double score =
                Math.min(
                        magnitude,
                        100.0
                );

        // Very small movements get lower priority
        if (magnitude < 2) {

            score *= 0.35;

        } else if (magnitude < 5) {

            score *= 0.60;

        } else if (magnitude < 10) {

            score *= 0.80;
        }

        return round(
                Math.min(
                        score,
                        100.0
                )
        );
    }

    // =========================================================
    // IMPACT LEVEL
    // =========================================================

    private String getImpactLevel(
            double score
    ) {

        if (score >= 70) {

            return "High";

        } else if (score >= 40) {

            return "Medium";

        } else {

            return "Low";
        }
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safeString(
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
}