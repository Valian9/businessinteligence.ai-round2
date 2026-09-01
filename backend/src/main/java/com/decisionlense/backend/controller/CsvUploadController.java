package com.decisionlense.backend.controller;
import com.decisionlense.backend.model.*;

import com.decisionlense.backend.service.LlmService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/csv")
@CrossOrigin(origins = "*")
public class CsvUploadController {

    private final LlmService llmService;

    public CsvUploadController(LlmService llmService) {
        this.llmService = llmService;
    }

    // ============================================================
    // UPLOAD
    // ============================================================

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCSV(
            @RequestParam("file") MultipartFile file) {

        try {

            // =====================================================
            // FILE VALIDATION
            // =====================================================

            if (file == null || file.isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error",
                                "Business data file is empty."
                        ));
            }

            String fileName =
                    file.getOriginalFilename() == null
                            ? "uploaded.csv"
                            : file.getOriginalFilename();

            String lowerFileName =
                    fileName.toLowerCase(Locale.ROOT);

            // =====================================================
            // READ FILE
            // =====================================================

            List<String[]> rows;

            if (lowerFileName.endsWith(".csv")) {

                rows = readCSVFile(file);

            } else if (
                    lowerFileName.endsWith(".xlsx") ||
                    lowerFileName.endsWith(".xls")) {

                rows = readExcelFile(file);

            } else {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error",
                                "Unsupported file type. Please upload CSV, XLS or XLSX."
                        ));
            }

            // =====================================================
            // BASIC VALIDATION
            // =====================================================

            if (rows.isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error",
                                "No data found in uploaded file."
                        ));
            }

            if (rows.size() < 2) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error",
                                "File must contain headers and at least one data row."
                        ));
            }

            // =====================================================
            // NORMALIZE ROW WIDTH
            // =====================================================

            String[] rawHeaders = rows.get(0);

            int columnCount = rawHeaders.length;

            List<String[]> normalizedRows =
                    normalizeRows(rows, columnCount);

            String[] headers =
                    new String[columnCount];

            for (int i = 0; i < columnCount; i++) {

                String original =
                        rawHeaders[i];

                headers[i] =
                        cleanHeader(original);

                if (headers[i].isEmpty()) {
                    headers[i] = "column_" + (i + 1);
                }
            }

            // =====================================================
            // DEBUG
            // =====================================================

            System.out.println();
            System.out.println("================================================");
            System.out.println("DECISIONLENS AI - FILE ANALYSIS");
            System.out.println("================================================");
            System.out.println("FILE      : " + fileName);
            System.out.println("HEADERS   : " + Arrays.toString(headers));
            System.out.println("ROWS      : " + (normalizedRows.size() - 1));
            System.out.println("================================================");

            // =====================================================
            // SMART COLUMN DETECTION
            // =====================================================

            int revenueIndex =
                    findSemanticColumn(
                            headers,
                            revenueAliases()
                    );

            int quantityIndex =
                    findSemanticColumn(
                            headers,
                            quantityAliases()
                    );

            int categoryIndex =
                    findSemanticColumn(
                            headers,
                            productAliases()
                    );

            int complaintsIndex =
                    findSemanticColumn(
                            headers,
                            complaintAliases()
                    );

            int inventoryIndex =
                    findSemanticColumn(
                            headers,
                            inventoryAliases()
                    );

            int stockoutIndex =
                    findSemanticColumn(
                            headers,
                            stockoutAliases()
                    );

            int dateIndex =
                    findSemanticColumn(
                            headers,
                            dateAliases()
                    );

            int regionIndex =
                    findSemanticColumn(
                            headers,
                            regionAliases()
                    );

            // =====================================================
            // NUMERIC COLUMNS
            // =====================================================

            List<Integer> numericColumns =
                    detectNumericColumns(
                            headers,
                            normalizedRows
                    );

            // =====================================================
            // SMART FALLBACKS
            // =====================================================

            /*
             * IMPORTANT:
             *
             * We DO NOT automatically call every numeric column revenue.
             *
             * Example:
             *
             * Product | Stock | StockoutDays
             *
             * This is an inventory dataset.
             *
             * Stock must remain Stock.
             * StockoutDays must remain StockoutDays.
             */

            if (revenueIndex == -1) {

                revenueIndex =
                        detectRevenueByScoring(
                                headers,
                                numericColumns,
                                quantityIndex,
                                inventoryIndex,
                                complaintsIndex,
                                stockoutIndex
                        );
            }

            if (quantityIndex == -1) {

                quantityIndex =
                        detectQuantityByScoring(
                                headers,
                                numericColumns,
                                revenueIndex,
                                inventoryIndex,
                                complaintsIndex,
                                stockoutIndex
                        );
            }

            if (categoryIndex == -1) {

                categoryIndex =
                        detectBestTextColumn(
                                headers,
                                normalizedRows,
                                dateIndex,
                                regionIndex
                        );
            }

            // =====================================================
            // DATASET TYPE
            // =====================================================

            String datasetType =
                    detectDatasetType(
                            revenueIndex,
                            quantityIndex,
                            inventoryIndex,
                            stockoutIndex,
                            complaintsIndex
                    );

            // =====================================================
            // DEBUG MAPPING
            // =====================================================

            System.out.println();
            System.out.println("================================================");
            System.out.println("SMART COLUMN MAPPING");
            System.out.println("================================================");

            System.out.println(
                    "Dataset   : " + datasetType
            );

            System.out.println(
                    "Revenue   : " +
                            getColumnName(headers, revenueIndex)
            );

            System.out.println(
                    "Quantity  : " +
                            getColumnName(headers, quantityIndex)
            );

            System.out.println(
                    "Product   : " +
                            getColumnName(headers, categoryIndex)
            );

            System.out.println(
                    "Complaints: " +
                            getColumnName(headers, complaintsIndex)
            );

            System.out.println(
                    "Inventory : " +
                            getColumnName(headers, inventoryIndex)
            );

            System.out.println(
                    "Stockout  : " +
                            getColumnName(headers, stockoutIndex)
            );

            System.out.println(
                    "Date      : " +
                            getColumnName(headers, dateIndex)
            );

            System.out.println(
                    "Region    : " +
                            getColumnName(headers, regionIndex)
            );

            System.out.println(
                    "Numeric   : " +
                            getColumnNames(headers, numericColumns)
            );

            System.out.println("================================================");

            // =====================================================
            // TOTALS
            // =====================================================

            double totalRevenue = 0;
            double totalQuantity = 0;
            double totalComplaints = 0;
            double totalInventory = 0;
            double totalStockoutDays = 0;

            int validRows = 0;
            int invalidRows = 0;

            // =====================================================
            // CATEGORY AGGREGATION
            // =====================================================

            Map<String, Double> categoryPrimary =
                    new LinkedHashMap<>();

            Map<String, Double> categoryRevenue =
                    new LinkedHashMap<>();

            Map<String, Double> categoryQuantity =
                    new LinkedHashMap<>();

            Map<String, Double> categoryInventory =
                    new LinkedHashMap<>();

            Map<String, Double> categoryStockout =
                    new LinkedHashMap<>();

            // =====================================================
            // PROCESS ROWS
            // =====================================================

            for (int i = 1; i < normalizedRows.size(); i++) {

                String[] row =
                        normalizedRows.get(i);

                try {

                    if (row == null ||
                            row.length == 0) {

                        invalidRows++;
                        continue;
                    }

                    // =================================================
                    // CATEGORY / PRODUCT
                    // =================================================

                    String category =
                            "Row " + i;

                    if (categoryIndex != -1 &&
                            categoryIndex < row.length) {

                        String value =
                                cleanTextValue(
                                        row[categoryIndex]
                                );

                        if (!value.isEmpty()) {
                            category = value;
                        }
                    }

                    // =================================================
                    // VALUES
                    // =================================================

                    double revenue =
                            getNumericValue(
                                    row,
                                    revenueIndex
                            );

                    double quantity =
                            getNumericValue(
                                    row,
                                    quantityIndex
                            );

                    double complaints =
                            getNumericValue(
                                    row,
                                    complaintsIndex
                            );

                    double inventory =
                            getNumericValue(
                                    row,
                                    inventoryIndex
                            );

                    double stockout =
                            getNumericValue(
                                    row,
                                    stockoutIndex
                            );

                    // =================================================
                    // TOTALS
                    // =================================================

                    totalRevenue += revenue;
                    totalQuantity += quantity;
                    totalComplaints += complaints;
                    totalInventory += inventory;
                    totalStockoutDays += stockout;

                    // =================================================
                    // CATEGORY METRICS
                    // =================================================

                    categoryRevenue.put(
                            category,
                            categoryRevenue.getOrDefault(
                                    category,
                                    0.0
                            ) + revenue
                    );

                    categoryQuantity.put(
                            category,
                            categoryQuantity.getOrDefault(
                                    category,
                                    0.0
                            ) + quantity
                    );

                    categoryInventory.put(
                            category,
                            categoryInventory.getOrDefault(
                                    category,
                                    0.0
                            ) + inventory
                    );

                    categoryStockout.put(
                            category,
                            categoryStockout.getOrDefault(
                                    category,
                                    0.0
                            ) + stockout
                    );

                    // =================================================
                    // PRIMARY VALUE
                    // =================================================

                    double primaryValue =
                            choosePrimaryValue(
                                    revenue,
                                    quantity,
                                    inventory,
                                    complaints,
                                    stockout,
                                    revenueIndex,
                                    quantityIndex,
                                    inventoryIndex,
                                    complaintsIndex,
                                    stockoutIndex
                            );

                    categoryPrimary.put(
                            category,
                            categoryPrimary.getOrDefault(
                                    category,
                                    0.0
                            ) + primaryValue
                    );

                    validRows++;

                } catch (Exception e) {

                    invalidRows++;

                    System.out.println(
                            "Skipped row " +
                                    i +
                                    ": " +
                                    Arrays.toString(row)
                    );
                }
            }

            // =====================================================
            // DATA QUALITY
            // =====================================================

            int totalRows =
                    normalizedRows.size() - 1;

            double dataQuality =
                    totalRows == 0
                            ? 0
                            : ((double) validRows /
                            totalRows) * 100.0;

            // =====================================================
            // PRIMARY KPI
            // =====================================================

            String primaryKPI =
                    buildPrimaryKPI(
                            datasetType,
                            headers,
                            revenueIndex,
                            quantityIndex,
                            inventoryIndex,
                            complaintsIndex,
                            stockoutIndex
                    );

            // =====================================================
            // PRIMARY TOTAL
            // =====================================================

            double primaryValue =
                    choosePrimaryTotal(
                            totalRevenue,
                            totalQuantity,
                            totalInventory,
                            totalComplaints,
                            totalStockoutDays,
                            revenueIndex,
                            quantityIndex,
                            inventoryIndex,
                            complaintsIndex,
                            stockoutIndex
                    );

            // =====================================================
            // TOP CONTRIBUTOR
            // =====================================================

            String topCategory = "N/A";
            double topCategoryValue = 0;

            for (Map.Entry<String, Double> entry :
                    categoryPrimary.entrySet()) {

                if (entry.getValue() >
                        topCategoryValue) {

                    topCategory =
                            entry.getKey();

                    topCategoryValue =
                            entry.getValue();
                }
            }

            double topShare =
                    primaryValue == 0
                            ? 0
                            : (topCategoryValue /
                            primaryValue) * 100.0;

            // =====================================================
            // DRIVER
            // =====================================================

            String driver =
                    determineDriver(
                            datasetType,
                            totalRevenue,
                            totalQuantity,
                            totalComplaints,
                            totalInventory,
                            totalStockoutDays
                    );

            // =====================================================
            // CONFIDENCE
            // =====================================================

            double confidence;

            if (validRows < 3) {

                confidence = 0.35;

            } else if (dataQuality < 70) {

                confidence = 0.50;

            } else if (validRows < 10) {

                confidence = 0.70;

            } else {

                confidence = 0.85;
            }

            boolean abstain =
                    validRows < 3 ||
                            dataQuality < 50;

            // =====================================================
            // EXECUTIVE INSIGHT
            // =====================================================

            String executiveInsight;

            if (abstain) {

                executiveInsight =
                        "The dataset contains limited validated evidence. " +
                        "Additional data is recommended before making a high-impact decision.";

            } else {

                executiveInsight =
                        topCategory +
                        " is the largest contributor to " +
                        primaryKPI +
                        ", representing approximately " +
                        String.format(
                                Locale.US,
                                "%.1f",
                                topShare
                        ) +
                        "% of the observed total. " +
                        "The primary observed driver is " +
                        driver +
                        ".";
            }

            // =====================================================
            // RECOMMENDATION
            // =====================================================

            String recommendation =
                    buildRecommendation(
                            datasetType,
                            totalRevenue,
                            totalQuantity,
                            totalComplaints,
                            totalInventory,
                            totalStockoutDays,
                            topCategory
                    );

            // =====================================================
            // LLM INPUT
            // =====================================================

            Map<String, Object> llmInput =
                    new LinkedHashMap<>();

            llmInput.put(
                    "fileName",
                    fileName
            );

            llmInput.put(
                    "datasetType",
                    datasetType
            );

            llmInput.put(
                    "primaryKPI",
                    primaryKPI
            );

            llmInput.put(
                    "primaryValue",
                    round(primaryValue)
            );

            llmInput.put(
                    "totalRevenue",
                    round(totalRevenue)
            );

            llmInput.put(
                    "totalQuantity",
                    round(totalQuantity)
            );

            llmInput.put(
                    "totalComplaints",
                    round(totalComplaints)
            );

            llmInput.put(
                    "totalInventory",
                    round(totalInventory)
            );

            llmInput.put(
                    "totalStockoutDays",
                    round(totalStockoutDays)
            );

            llmInput.put(
                    "topContributor",
                    topCategory
            );

            llmInput.put(
                    "topContributorValue",
                    round(topCategoryValue)
            );

            llmInput.put(
                    "topContributionPercent",
                    round(topShare)
            );

            llmInput.put(
                    "driver",
                    driver
            );

            llmInput.put(
                    "confidence",
                    confidence
            );

            llmInput.put(
                    "validRows",
                    validRows
            );

            llmInput.put(
                    "invalidRows",
                    invalidRows
            );

            llmInput.put(
                    "dataQuality",
                    round(dataQuality)
            );

            llmInput.put(
                    "detectedColumns",
                    Arrays.asList(headers)
            );

            // =====================================================
            // LLM
            // =====================================================

            Map<String, Object> llmResult =
                    new LinkedHashMap<>();

            try {

                llmResult =
                        llmService.generateBusinessNarrative(
                                llmInput
                        );

            } catch (Exception llmException) {

                System.out.println(
                        "LLM service unavailable: " +
                                llmException.getMessage()
                );

                llmResult.put(
                        "executiveInsight",
                        executiveInsight
                );

                llmResult.put(
                        "recommendation",
                        recommendation
                );
            }

            // =====================================================
            // RESPONSE
            // =====================================================

            Map<String, Object> response =
                    new LinkedHashMap<>();

            response.put(
                    "fileName",
                    fileName
            );

            response.put(
                    "datasetType",
                    datasetType
            );

            response.put(
                    "kpi",
                    primaryKPI
            );

            response.put(
                    "primaryValue",
                    round(primaryValue)
            );

            response.put(
                    "totalRevenue",
                    round(totalRevenue)
            );

            response.put(
                    "totalQuantity",
                    round(totalQuantity)
            );

            response.put(
                    "totalComplaints",
                    round(totalComplaints)
            );

            response.put(
                    "totalInventory",
                    round(totalInventory)
            );

            response.put(
                    "totalStockoutDays",
                    round(totalStockoutDays)
            );

            response.put(
                    "categoryCount",
                    categoryPrimary.size()
            );

            response.put(
                    "topCategory",
                    topCategory
            );

            response.put(
                    "topCategoryRevenue",
                    round(
                            revenueIndex != -1
                                    ? categoryRevenue.getOrDefault(
                                    topCategory,
                                    0.0
                            )
                                    : topCategoryValue
                    )
            );

            response.put(
                    "topContributionPercent",
                    round(topShare)
            );

            response.put(
                    "driver",
                    driver
            );

            response.put(
                    "confidence",
                    confidence
            );

            response.put(
                    "confidenceStatus",
                    abstain
                            ? "LOW_CONFIDENCE / ABSTAIN"
                            : "SUFFICIENT_EVIDENCE"
            );

            response.put(
                    "abstained",
                    abstain
            );

            response.put(
                    "analyticalMethod",
                    "Deterministic Java + Semantic Column Detection"
            );

            response.put(
                    "aiLayer",
                    "LLM narrative + recommendation layer"
            );

            response.put(
                    "llm",
                    llmResult
            );

            response.put(
                    "executiveInsight",
                    executiveInsight
            );

            response.put(
                    "recommendation",
                    recommendation
            );

            response.put(
                    "validRows",
                    validRows
            );

            response.put(
                    "invalidRows",
                    invalidRows
            );

            response.put(
                    "dataQuality",
                    round(dataQuality)
            );

            // =====================================================
            // CATEGORY RESULTS
            // =====================================================

            response.put(
                    "categories",
                    buildCategoryResults(
                            categoryPrimary,
                            categoryRevenue,
                            categoryQuantity,
                            categoryInventory,
                            categoryStockout,
                            revenueIndex,
                            quantityIndex,
                            inventoryIndex,
                            stockoutIndex
                    )
            );

            // =====================================================
            // DETECTED COLUMNS
            // =====================================================

            response.put(
                    "detectedColumns",
                    Arrays.asList(headers)
            );

            response.put(
                    "autoDetected",
                    true
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Unable to process business data: " +
                                    e.getMessage()
                    ));
        }
    }

    // ============================================================
    // DATASET TYPE
    // ============================================================

    private String detectDatasetType(
            int revenueIndex,
            int quantityIndex,
            int inventoryIndex,
            int stockoutIndex,
            int complaintsIndex) {

        if (revenueIndex != -1 ||
                quantityIndex != -1) {

            return "SALES";
        }

        if (inventoryIndex != -1 ||
                stockoutIndex != -1) {

            return "INVENTORY";
        }

        if (complaintsIndex != -1) {

            return "CUSTOMER_EXPERIENCE";
        }

        return "GENERAL_BUSINESS";
    }

    // ============================================================
    // PRIMARY KPI
    // ============================================================

    private String buildPrimaryKPI(
            String datasetType,
            String[] headers,
            int revenueIndex,
            int quantityIndex,
            int inventoryIndex,
            int complaintsIndex,
            int stockoutIndex) {

        if (datasetType.equals("SALES")) {

            if (revenueIndex != -1) {

                return "Revenue / " +
                        headers[revenueIndex];
            }

            if (quantityIndex != -1) {

                return "Sales Volume / " +
                        headers[quantityIndex];
            }
        }

        if (datasetType.equals("INVENTORY")) {

            if (inventoryIndex != -1) {

                return "Inventory / " +
                        headers[inventoryIndex];
            }

            if (stockoutIndex != -1) {

                return "Stockout Days / " +
                        headers[stockoutIndex];
            }
        }

        if (datasetType.equals("CUSTOMER_EXPERIENCE") &&
                complaintsIndex != -1) {

            return "Customer Complaints / " +
                    headers[complaintsIndex];
        }

        return "Business KPI";
    }

    // ============================================================
    // PRIMARY VALUE
    // ============================================================

    private double choosePrimaryValue(
            double revenue,
            double quantity,
            double inventory,
            double complaints,
            double stockout,
            int revenueIndex,
            int quantityIndex,
            int inventoryIndex,
            int complaintsIndex,
            int stockoutIndex) {

        if (revenueIndex != -1) {
            return revenue;
        }

        if (quantityIndex != -1) {
            return quantity;
        }

        if (inventoryIndex != -1) {
            return inventory;
        }

        if (complaintsIndex != -1) {
            return complaints;
        }

        if (stockoutIndex != -1) {
            return stockout;
        }

        return 0;
    }

    // ============================================================
    // PRIMARY TOTAL
    // ============================================================

    private double choosePrimaryTotal(
            double revenue,
            double quantity,
            double inventory,
            double complaints,
            double stockout,
            int revenueIndex,
            int quantityIndex,
            int inventoryIndex,
            int complaintsIndex,
            int stockoutIndex) {

        if (revenueIndex != -1) {
            return revenue;
        }

        if (quantityIndex != -1) {
            return quantity;
        }

        if (inventoryIndex != -1) {
            return inventory;
        }

        if (complaintsIndex != -1) {
            return complaints;
        }

        if (stockoutIndex != -1) {
            return stockout;
        }

        return 0;
    }

    // ============================================================
    // DRIVER
    // ============================================================

    private String determineDriver(
            String datasetType,
            double revenue,
            double quantity,
            double complaints,
            double inventory,
            double stockout) {

        if (datasetType.equals("INVENTORY")) {

            if (stockout > 0) {
                return "Stock Availability + Stockout Risk";
            }

            return "Inventory Availability";
        }

        if (datasetType.equals("SALES")) {

            if (revenue > 0 &&
                    quantity > 0) {

                return "Sales Volume + Revenue/Product Mix";
            }

            if (revenue > 0) {
                return "Revenue Contribution";
            }

            return "Sales Volume";
        }

        if (complaints > 0) {

            return "Customer Complaints + Product Performance";
        }

        if (inventory > 0) {

            return "Inventory + Product Performance";
        }

        return "Primary KPI Contribution";
    }

    // ============================================================
    // RECOMMENDATION
    // ============================================================

    private String buildRecommendation(
            String datasetType,
            double revenue,
            double quantity,
            double complaints,
            double inventory,
            double stockout,
            String topCategory) {

        if (datasetType.equals("INVENTORY")) {

            if (stockout > 0) {

                return "Prioritize products with higher stockout exposure, " +
                        "review replenishment levels and align inventory " +
                        "with observed product demand.";
            }

            return "Review inventory concentration and maintain stock " +
                    "levels according to product demand.";
        }

        if (datasetType.equals("SALES")) {

            if (revenue > 0 &&
                    quantity > 0) {

                return "Investigate the strongest revenue contributors, " +
                        "compare sales volume with revenue performance and " +
                        "optimize product mix.";
            }

            if (revenue > 0) {

                return "Review the strongest revenue contributors and " +
                        "evaluate pricing, demand and product-mix opportunities.";
            }

            return "Monitor sales volume and identify products with " +
                    "the strongest contribution.";
        }

        if (complaints > 0) {

            return "Review products or segments associated with higher " +
                    "complaint levels and investigate customer experience issues.";
        }

        return "Monitor the primary KPI over time and investigate " +
                topCategory +
                " as the strongest observed contributor.";
    }

    // ============================================================
    // CATEGORY RESULTS
    // ============================================================

    private List<Map<String, Object>> buildCategoryResults(
            Map<String, Double> primaryValues,
            Map<String, Double> revenueValues,
            Map<String, Double> quantityValues,
            Map<String, Double> inventoryValues,
            Map<String, Double> stockoutValues,
            int revenueIndex,
            int quantityIndex,
            int inventoryIndex,
            int stockoutIndex) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (String category :
                primaryValues.keySet()) {

            Map<String, Object> item =
                    new LinkedHashMap<>();

            item.put(
                    "category",
                    category
            );

            item.put(
                    "product",
                    category
            );

            double primary =
                    primaryValues.getOrDefault(
                            category,
                            0.0
                    );

            double revenue =
                    revenueValues.getOrDefault(
                            category,
                            0.0
                    );

            double quantity =
                    quantityValues.getOrDefault(
                            category,
                            0.0
                    );

            double inventory =
                    inventoryValues.getOrDefault(
                            category,
                            0.0
                    );

            double stockout =
                    stockoutValues.getOrDefault(
                            category,
                            0.0
                    );

            /*
             * Frontend compatibility:
             *
             * "value" always represents the relevant
             * primary metric.
             */

            item.put(
                    "value",
                    round(primary)
            );

            item.put(
                    "revenue",
                    round(revenue)
            );

            item.put(
                    "quantity",
                    round(quantity)
            );

            item.put(
                    "inventory",
                    round(inventory)
            );

            item.put(
                    "stockoutDays",
                    round(stockout)
            );

            result.add(item);
        }

        result.sort(
                (a, b) ->
                        Double.compare(
                                ((Number) b.get("value"))
                                        .doubleValue(),
                                ((Number) a.get("value"))
                                        .doubleValue()
                        )
        );

        return result;
    }

    // ============================================================
    // REVENUE DETECTION
    // ============================================================

    private int detectRevenueByScoring(
            String[] headers,
            List<Integer> numericColumns,
            int quantityIndex,
            int inventoryIndex,
            int complaintsIndex,
            int stockoutIndex) {

        String[] strongWords = {
                "revenue",
                "sales revenue",
                "sales amount",
                "sales value",
                "net sales",
                "gross sales",
                "total sales",
                "amount",
                "order amount",
                "order value",
                "transaction amount",
                "income",
                "turnover"
        };

        String[] weakWords = {
                "sales",
                "value",
                "amount",
                "price",
                "total",
                "cost"
        };

        int bestIndex = -1;
        int bestScore = 0;

        for (Integer index :
                numericColumns) {

            if (index == quantityIndex ||
                    index == inventoryIndex ||
                    index == complaintsIndex ||
                    index == stockoutIndex) {

                continue;
            }

            String header =
                    headers[index];

            int score = 0;

            for (String word :
                    strongWords) {

                if (header.contains(
                        cleanHeader(word)
                )) {

                    score += 100;
                }
            }

            for (String word :
                    weakWords) {

                if (header.contains(
                        cleanHeader(word)
                )) {

                    score += 20;
                }
            }

            // Explicitly reject inventory-like columns.
            if (containsAny(
                    header,
                    inventoryAliases()
            )) {

                score -= 150;
            }

            if (containsAny(
                    header,
                    stockoutAliases()
            )) {

                score -= 150;
            }

            if (score > bestScore) {

                bestScore = score;
                bestIndex = index;
            }
        }

        /*
         * No arbitrary numeric fallback.
         *
         * This is important for inventory-only datasets.
         */

        return bestScore >= 40
                ? bestIndex
                : -1;
    }

    // ============================================================
    // QUANTITY DETECTION
    // ============================================================

    private int detectQuantityByScoring(
            String[] headers,
            List<Integer> numericColumns,
            int revenueIndex,
            int inventoryIndex,
            int complaintsIndex,
            int stockoutIndex) {

        String[] quantityWords = {
                "quantity",
                "qty",
                "units",
                "unit sold",
                "units sold",
                "sales quantity",
                "quantity sold",
                "volume",
                "order quantity",
                "items sold",
                "item quantity",
                "number sold"
        };

        int bestIndex = -1;
        int bestScore = 0;

        for (Integer index :
                numericColumns) {

            if (index == revenueIndex ||
                    index == inventoryIndex ||
                    index == complaintsIndex ||
                    index == stockoutIndex) {

                continue;
            }

            String header =
                    headers[index];

            int score = 0;

            for (String word :
                    quantityWords) {

                if (header.contains(
                        cleanHeader(word)
                )) {

                    score += 100;
                }
            }

            if (containsAny(
                    header,
                    inventoryAliases()
            )) {

                score -= 150;
            }

            if (containsAny(
                    header,
                    stockoutAliases()
            )) {

                score -= 150;
            }

            if (score > bestScore) {

                bestScore = score;
                bestIndex = index;
            }
        }

        return bestScore >= 40
                ? bestIndex
                : -1;
    }

    // ============================================================
    // NUMERIC COLUMN DETECTION
    // ============================================================

    private List<Integer> detectNumericColumns(
            String[] headers,
            List<String[]> rows) {

        List<Integer> result =
                new ArrayList<>();

        for (int column = 0;
             column < headers.length;
             column++) {

            int numeric = 0;
            int nonEmpty = 0;

            for (int rowIndex = 1;
                 rowIndex < rows.size();
                 rowIndex++) {

                String[] row =
                        rows.get(rowIndex);

                if (row == null ||
                        column >= row.length) {

                    continue;
                }

                String value =
                        cleanValue(
                                row[column]
                        );

                if (value.isEmpty()) {
                    continue;
                }

                nonEmpty++;

                if (isNumeric(value)) {
                    numeric++;
                }
            }

            if (nonEmpty > 0 &&
                    ((double) numeric /
                            nonEmpty) >= 0.60) {

                result.add(column);
            }
        }

        return result;
    }

    // ============================================================
    // TEXT COLUMN DETECTION
    // ============================================================

    private int detectBestTextColumn(
            String[] headers,
            List<String[]> rows,
            int dateIndex,
            int regionIndex) {

        int best = -1;
        int bestScore = -1;

        String[] preferredWords = {
                "product",
                "item",
                "category",
                "service",
                "sku",
                "name",
                "model",
                "product name",
                "item name"
        };

        for (int column = 0;
             column < headers.length;
             column++) {

            if (column == dateIndex ||
                    column == regionIndex) {

                continue;
            }

            String header =
                    headers[column];

            int score = 0;

            // Strong semantic match
            for (String word :
                    preferredWords) {

                if (header.contains(
                        cleanHeader(word)
                )) {

                    score += 100;
                }
            }

            // Avoid obvious non-category columns
            if (containsAny(
                    header,
                    dateAliases()
            )) {

                score -= 100;
            }

            if (containsAny(
                    header,
                    regionAliases()
            )) {

                score -= 100;
            }

            if (containsAny(
                    header,
                    revenueAliases()
            )) {

                score -= 100;
            }

            if (containsAny(
                    header,
                    quantityAliases()
            )) {

                score -= 100;
            }

            if (containsAny(
                    header,
                    inventoryAliases()
            )) {

                score -= 100;
            }

            if (containsAny(
                    header,
                    stockoutAliases()
            )) {

                score -= 100;
            }

            Set<String> unique =
                    new HashSet<>();

            int nonEmpty = 0;

            for (int rowIndex = 1;
                 rowIndex < rows.size();
                 rowIndex++) {

                String[] row =
                        rows.get(rowIndex);

                if (row == null ||
                        column >= row.length) {

                    continue;
                }

                String value =
                        cleanTextValue(
                                row[column]
                        );

                if (value.isEmpty()) {
                    continue;
                }

                if (!isNumeric(value)) {

                    nonEmpty++;
                    unique.add(
                            value.toLowerCase(
                                    Locale.ROOT
                            )
                    );
                }
            }

            if (nonEmpty > 0) {

                score +=
                        Math.min(
                                unique.size(),
                                50
                        );
            }

            if (score > bestScore) {

                bestScore = score;
                best = column;
            }
        }

        return best;
    }

    // ============================================================
    // SEMANTIC COLUMN FINDER
    // ============================================================

    private int findSemanticColumn(
            String[] headers,
            String[] aliases) {

        // ========================================================
        // PASS 1 - EXACT MATCH
        // ========================================================

        for (String alias :
                aliases) {

            String target =
                    cleanHeader(alias);

            for (int i = 0;
                 i < headers.length;
                 i++) {

                if (headers[i].equals(target)) {

                    return i;
                }
            }
        }

        // ========================================================
        // PASS 2 - SAFE CONTAINS
        // ========================================================

        int bestIndex = -1;
        int bestScore = 0;

        for (int i = 0;
             i < headers.length;
             i++) {

            String header =
                    headers[i];

            for (String alias :
                    aliases) {

                String target =
                        cleanHeader(alias);

                int score = 0;

                if (header.contains(target)) {

                    score =
                            target.length() >= 6
                                    ? 80
                                    : 50;
                }

                if (score > bestScore) {

                    bestScore = score;
                    bestIndex = i;
                }
            }
        }

        return bestScore >= 50
                ? bestIndex
                : -1;
    }

    // ============================================================
    // ALIASES
    // ============================================================

    private String[] revenueAliases() {

        return new String[] {
                "revenue",
                "total revenue",
                "sales revenue",
                "revenue amount",
                "revenue value",
                "sales amount",
                "sales value",
                "total sales",
                "net sales",
                "gross sales",
                "sales income",
                "income",
                "turnover",
                "order value",
                "order amount",
                "transaction amount",
                "transaction value"
        };
    }

    private String[] quantityAliases() {

        return new String[] {
                "quantity",
                "qty",
                "units",
                "units sold",
                "unit sold",
                "quantity sold",
                "sales quantity",
                "sales volume",
                "order quantity",
                "units purchased",
                "items sold",
                "item quantity",
                "number sold",
                "volume sold"
        };
    }

    private String[] productAliases() {

        return new String[] {
                "category",
                "product",
                "product name",
                "productname",
                "item",
                "item name",
                "itemname",
                "sku",
                "sku name",
                "product id",
                "product code",
                "service",
                "service name",
                "model",
                "model name"
        };
    }

    private String[] complaintAliases() {

        return new String[] {
                "complaints",
                "complaint",
                "customer complaints",
                "customer complaint",
                "customer issues",
                "customer issue",
                "issues",
                "returns",
                "return count",
                "return quantity",
                "refund count"
        };
    }

    private String[] inventoryAliases() {

        return new String[] {
                "inventory",
                "stock",
                "stock level",
                "available stock",
                "stock quantity",
                "inventory level",
                "units in stock",
                "current stock",
                "stock on hand",
                "on hand inventory",
                "on hand stock",
                "available inventory"
        };
    }

    private String[] stockoutAliases() {

        return new String[] {
                "stockoutdays",
                "stockout days",
                "stock out days",
                "out of stock days",
                "stockout",
                "stock out",
                "days out of stock",
                "oos days",
                "outofstockdays"
        };
    }

    private String[] dateAliases() {

        return new String[] {
                "date",
                "transaction date",
                "order date",
                "sales date",
                "invoice date",
                "purchase date",
                "day",
                "timestamp",
                "created at",
                "created date",
                "transaction time"
        };
    }

    private String[] regionAliases() {

        return new String[] {
                "region",
                "area",
                "location",
                "zone",
                "market",
                "city",
                "state",
                "territory",
                "branch",
                "district"
        };
    }

    // ============================================================
    // ALIAS CHECK
    // ============================================================

    private boolean containsAny(
            String header,
            String[] aliases) {

        for (String alias :
                aliases) {

            String cleaned =
                    cleanHeader(alias);

            if (header.equals(cleaned) ||
                    header.contains(cleaned)) {

                return true;
            }
        }

        return false;
    }

    // ============================================================
    // GET NUMERIC VALUE
    // ============================================================

    private double getNumericValue(
            String[] row,
            int index) {

        if (index < 0 ||
                index >= row.length) {

            return 0;
        }

        return parseNumberSafe(
                row[index]
        );
    }

    // ============================================================
    // COLUMN NAME
    // ============================================================

    private String getColumnName(
            String[] headers,
            int index) {

        if (index < 0 ||
                index >= headers.length) {

            return "Not available";
        }

        return headers[index];
    }

    // ============================================================
    // COLUMN NAMES
    // ============================================================

    private List<String> getColumnNames(
            String[] headers,
            List<Integer> indexes) {

        List<String> result =
                new ArrayList<>();

        for (Integer index :
                indexes) {

            if (index != null &&
                    index >= 0 &&
                    index < headers.length) {

                result.add(
                        headers[index]
                );
            }
        }

        return result;
    }

    // ============================================================
    // READ CSV
    // ============================================================

    private List<String[]> readCSVFile(
            MultipartFile file) throws Exception {

        List<String[]> rows =
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

            String line;

            while ((line = reader.readLine()) != null) {

                if (!line.trim().isEmpty()) {

                    rows.add(
                            parseCSVLine(line)
                    );
                }
            }
        }

        return rows;
    }

    // ============================================================
    // READ EXCEL
    // ============================================================

    private List<String[]> readExcelFile(
            MultipartFile file) throws Exception {

        List<String[]> rows =
                new ArrayList<>();

        try (
                InputStream inputStream =
                        file.getInputStream();

                Workbook workbook =
                        WorkbookFactory.create(
                                inputStream
                        )
        ) {

            Sheet sheet =
                    workbook.getSheetAt(0);

            DataFormatter formatter =
                    new DataFormatter();

            for (Row row : sheet) {

                int lastCellNum =
                        row.getLastCellNum();

                if (lastCellNum < 0) {
                    continue;
                }

                List<String> values =
                        new ArrayList<>();

                for (int i = 0;
                     i < lastCellNum;
                     i++) {

                    Cell cell =
                            row.getCell(
                                    i,
                                    Row.MissingCellPolicy
                                            .CREATE_NULL_AS_BLANK
                            );

                    values.add(
                            formatter.formatCellValue(
                                    cell
                            )
                    );
                }

                boolean hasData =
                        values.stream()
                                .anyMatch(
                                        value ->
                                                value != null &&
                                                        !value.trim().isEmpty()
                                );

                if (hasData) {

                    rows.add(
                            values.toArray(
                                    new String[0]
                            )
                    );
                }
            }
        }

        return rows;
    }

    // ============================================================
    // NORMALIZE ROWS
    // ============================================================

    private List<String[]> normalizeRows(
            List<String[]> rows,
            int columnCount) {

        List<String[]> result =
                new ArrayList<>();

        for (String[] row :
                rows) {

            String[] normalized =
                    new String[columnCount];

            Arrays.fill(
                    normalized,
                    ""
            );

            if (row != null) {

                for (int i = 0;
                     i < Math.min(
                             row.length,
                             columnCount
                     );
                     i++) {

                    normalized[i] =
                            row[i] == null
                                    ? ""
                                    : row[i];
                }
            }

            result.add(normalized);
        }

        return result;
    }

    // ============================================================
    // HEADER CLEANING
    // ============================================================

    private String cleanHeader(
            String header) {

        if (header == null) {
            return "";
        }

        return header
                .replace("\uFEFF", "")
                .replace("\uFFFE", "")
                .replace("\"", "")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace("_", " ")
                .replace("-", " ")
                .replace("/", " ")
                .replace("\\", " ")
                .replaceAll("\\s+", " ");
    }

    // ============================================================
    // VALUE CLEANING
    // ============================================================

    private String cleanValue(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\uFEFF", "")
                .replace("\uFFFE", "")
                .replace("\"", "")
                .replace("₹", "")
                .replace("$", "")
                .replace("€", "")
                .replace("£", "")
                .replace(",", "")
                .replace("%", "")
                .trim();
    }

    // ============================================================
    // TEXT CLEANING
    // ============================================================

    private String cleanTextValue(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\uFEFF", "")
                .replace("\uFFFE", "")
                .replace("\"", "")
                .trim();
    }

    // ============================================================
    // NUMBER CHECK
    // ============================================================

    private boolean isNumeric(
            String value) {

        try {

            Double.parseDouble(
                    cleanValue(value)
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // ============================================================
    // SAFE NUMBER PARSER
    // ============================================================

    private double parseNumberSafe(
            String value) {

        String cleaned =
                cleanValue(value);

        if (cleaned.isEmpty()) {
            return 0;
        }

        try {

            return Double.parseDouble(
                    cleaned
            );

        } catch (Exception e) {

            return 0;
        }
    }

    // ============================================================
    // ROUND
    // ============================================================

    private double round(
            double value) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    // ============================================================
    // CSV PARSER
    // ============================================================

    private String[] parseCSVLine(
            String line) {

        List<String> values =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0;
             i < line.length();
             i++) {

            char c =
                    line.charAt(i);

            if (c == '"') {

                if (insideQuotes &&
                        i + 1 < line.length() &&
                        line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {

                    insideQuotes =
                            !insideQuotes;
                }

            } else if (
                    c == ',' &&
                    !insideQuotes) {

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
}