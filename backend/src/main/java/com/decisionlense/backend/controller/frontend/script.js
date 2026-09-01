const API = "http://localhost:8080/api";


// =====================================================
// PAGE LOAD
// =====================================================

document.addEventListener("DOMContentLoaded", () => {

    const analyzeBtn = document.getElementById("analyzeBtn");
    const fileInput = document.getElementById("csvFile");

    if (analyzeBtn) {
        analyzeBtn.addEventListener("click", analyzeCSV);
    }

    // Show selected filename
    if (fileInput) {
        fileInput.addEventListener("change", () => {

            const status =
                document.getElementById("uploadStatus");

            if (
                fileInput.files &&
                fileInput.files.length > 0
            ) {
                status.textContent =
                    "📁 Selected: " +
                    fileInput.files[0].name;
            }

        });
    }

});


// =====================================================
// MAIN SINGLE-FILE ANALYSIS
// =====================================================

async function analyzeCSV() {

    const fileInput =
        document.getElementById("csvFile");

    const status =
        document.getElementById("uploadStatus");

    const analyzeBtn =
        document.getElementById("analyzeBtn");


    if (
        !fileInput ||
        !fileInput.files ||
        fileInput.files.length === 0
    ) {

        status.textContent =
            "❌ Please select a CSV or Excel file first.";

        return;
    }


    const file =
        fileInput.files[0];

    const fileName =
        file.name.toLowerCase();


    const isSupported =
        fileName.endsWith(".csv") ||
        fileName.endsWith(".xlsx") ||
        fileName.endsWith(".xls");


    if (!isSupported) {

        status.textContent =
            "❌ Please upload a CSV, XLSX or XLS file.";

        return;
    }


    const formData =
        new FormData();

    formData.append(
        "file",
        file
    );


    status.textContent =
        "⏳ Analyzing " +
        file.name +
        "...";


    if (analyzeBtn) {

        analyzeBtn.disabled = true;

        analyzeBtn.textContent =
            "⏳ Analyzing...";

    }


    try {

        const response =
            await fetch(
                `${API}/csv/upload`,
                {
                    method: "POST",
                    body: formData
                }
            );


        const responseText =
            await response.text();


        let data;


        try {

            data =
                JSON.parse(responseText);

        }
        catch (error) {

            throw new Error(
                responseText ||
                "Backend returned an invalid response."
            );

        }


        if (!response.ok) {

            throw new Error(
                data.error ||
                data.message ||
                "File analysis failed."
            );

        }


        console.log(
            "DecisionLens Response:",
            data
        );


        // =================================================
        // CATEGORY COUNT
        // =================================================

        const categoryCount =
            Array.isArray(data.categories)
                ? data.categories.length
                : Number(
                    data.categoryCount || 0
                );


        // =================================================
        // DETECT KPI
        // =================================================

        const kpi =
            String(
                data.kpi || ""
            ).toLowerCase();


        let metricType =
            "revenue";


        if (
            kpi.includes("stockout") ||
            kpi.includes("out of stock")
        ) {

            metricType =
                "stockout";

        }

        else if (
            kpi.includes("inventory") ||
            kpi.includes("stock")
        ) {

            metricType =
                "inventory";

        }

        else if (
            kpi.includes("quantity") ||
            kpi.includes("units") ||
            kpi.includes("volume")
        ) {

            metricType =
                "quantity";

        }

        else if (
            kpi.includes("complaint") ||
            kpi.includes("return") ||
            kpi.includes("issue")
        ) {

            metricType =
                "complaints";

        }


        console.log(
            "Detected metric:",
            metricType
        );


        // =================================================
        // KPI ELEMENTS
        // =================================================

        const revenueElement =
            document.getElementById("revenue");

        const quantityElement =
            document.getElementById("quantity");

        const categoriesElement =
            document.getElementById("categories");

        const revenueLabel =
            document.getElementById("revenueLabel");

        const quantityLabel =
            document.getElementById("quantityLabel");

        const categoryLabel =
            document.getElementById("categoryLabel");

        const categoryTitle =
            document.getElementById("categoryTitle");


        // =================================================
        // DYNAMIC SINGLE-SOURCE KPI DISPLAY
        // =================================================

        if (metricType === "inventory") {

            if (revenueLabel) {
                revenueLabel.textContent =
                    "📦 Total Inventory";
            }

            if (quantityLabel) {
                quantityLabel.textContent =
                    "📅 Stockout Days";
            }

            if (categoryLabel) {
                categoryLabel.textContent =
                    "📊 Categories";
            }

            if (categoryTitle) {
                categoryTitle.textContent =
                    "📊 Inventory by Product";
            }

            if (revenueElement) {
                revenueElement.textContent =
                    Number(
                        data.totalInventory || 0
                    ).toLocaleString();
            }

            if (quantityElement) {
                quantityElement.textContent =
                    Number(
                        data.totalStockoutDays || 0
                    ).toLocaleString();
            }

        }


        else if (metricType === "quantity") {

            if (revenueLabel) {
                revenueLabel.textContent =
                    "📦 Total Quantity";
            }

            if (quantityLabel) {
                quantityLabel.textContent =
                    "📊 Categories";
            }

            if (categoryLabel) {
                categoryLabel.textContent =
                    "📊 Categories";
            }

            if (categoryTitle) {
                categoryTitle.textContent =
                    "📊 Quantity by Product";
            }

            if (revenueElement) {
                revenueElement.textContent =
                    Number(
                        data.totalQuantity || 0
                    ).toLocaleString();
            }

            if (quantityElement) {
                quantityElement.textContent =
                    categoryCount.toLocaleString();
            }

        }


        else if (metricType === "complaints") {

            if (revenueLabel) {
                revenueLabel.textContent =
                    "⚠️ Total Complaints";
            }

            if (quantityLabel) {
                quantityLabel.textContent =
                    "📊 Categories";
            }

            if (categoryLabel) {
                categoryLabel.textContent =
                    "📊 Categories";
            }

            if (categoryTitle) {
                categoryTitle.textContent =
                    "📊 Complaints by Category";
            }

            if (revenueElement) {
                revenueElement.textContent =
                    Number(
                        data.totalComplaints || 0
                    ).toLocaleString();
            }

            if (quantityElement) {
                quantityElement.textContent =
                    categoryCount.toLocaleString();
            }

        }


        else if (metricType === "stockout") {

            if (revenueLabel) {
                revenueLabel.textContent =
                    "⏱️ Total Stockout Hours";
            }

            if (quantityLabel) {
                quantityLabel.textContent =
                    "📊 Categories";
            }

            if (categoryLabel) {
                categoryLabel.textContent =
                    "📊 Categories";
            }

            if (categoryTitle) {
                categoryTitle.textContent =
                    "📊 Stockout by Product";
            }

            if (revenueElement) {
                revenueElement.textContent =
                    Number(
                        data.totalStockoutHours ??
                        data.totalStockoutDays ??
                        0
                    ).toLocaleString();
            }

            if (quantityElement) {
                quantityElement.textContent =
                    categoryCount.toLocaleString();
            }

        }


        else {

            if (revenueLabel) {
                revenueLabel.textContent =
                    "💰 Total Revenue";
            }

            if (quantityLabel) {
                quantityLabel.textContent =
                    "📦 Total Quantity";
            }

            if (categoryLabel) {
                categoryLabel.textContent =
                    "📊 Categories";
            }

            if (categoryTitle) {
                categoryTitle.textContent =
                    "📊 Category Revenue";
            }

            if (revenueElement) {
                revenueElement.textContent =
                    "₹" +
                    Number(
                        data.totalRevenue || 0
                    ).toLocaleString();
            }

            if (quantityElement) {
                quantityElement.textContent =
                    Number(
                        data.totalQuantity || 0
                    ).toLocaleString();
            }

        }


        // =================================================
        // CATEGORY COUNT
        // =================================================

        if (categoriesElement) {

            categoriesElement.textContent =
                categoryCount.toLocaleString();

        }


        // =================================================
        // CATEGORY / PRODUCT PERFORMANCE
        // =================================================

        const categoryContainer =
            document.getElementById(
                "categoryData"
            );


        if (categoryContainer) {

            categoryContainer.innerHTML = "";


            if (
                Array.isArray(data.categories) &&
                data.categories.length > 0
            ) {

                data.categories.forEach(
                    (item) => {

                        const div =
                            document.createElement(
                                "div"
                            );


                        div.className =
                            "category-item";


                        const name =
                            item.product ??
                            item.category ??
                            item.name ??
                            "Unknown";


                        const value =
                            Number(
                                item.value ??
                                item.revenue ??
                                item.quantity ??
                                0
                            );


                        let displayValue;


                        if (
                            metricType === "inventory" ||
                            metricType === "quantity"
                        ) {

                            displayValue =
                                value.toLocaleString() +
                                " units";

                        }

                        else if (
                            metricType === "complaints"
                        ) {

                            displayValue =
                                value.toLocaleString() +
                                " complaints";

                        }

                        else if (
                            metricType === "stockout"
                        ) {

                            displayValue =
                                value.toLocaleString() +
                                " hours";

                        }

                        else {

                            displayValue =
                                "₹" +
                                value.toLocaleString();

                        }


                        const nameSpan =
                            document.createElement(
                                "span"
                            );


                        nameSpan.textContent =
                            name;


                        const valueStrong =
                            document.createElement(
                                "strong"
                            );


                        valueStrong.textContent =
                            displayValue;


                        div.appendChild(
                            nameSpan
                        );


                        div.appendChild(
                            valueStrong
                        );


                        categoryContainer.appendChild(
                            div
                        );

                    }
                );

            }

            else {

                categoryContainer.innerHTML =
                    "<p>No category/contributor data available.</p>";

            }

        }


        // =================================================
        // SINGLE SOURCE AI INSIGHTS
        // =================================================

        const insightsElement =
            document.getElementById(
                "insights"
            );


        const aiNarrative =
            data.llm?.executiveInsight ||
            data.executiveInsight ||
            "AI narrative unavailable.";


        const aiRecommendation =
            data.llm?.recommendation ||
            data.recommendation ||
            "No recommendation available.";


        const topValue =
            Number(
                data.topCategoryValue ??
                data.topCategoryRevenue ??
                0
            );


        let topValueDisplay;


        if (
            metricType === "inventory" ||
            metricType === "quantity"
        ) {

            topValueDisplay =
                topValue.toLocaleString() +
                " units";

        }

        else if (
            metricType === "complaints"
        ) {

            topValueDisplay =
                topValue.toLocaleString() +
                " complaints";

        }

        else if (
            metricType === "stockout"
        ) {

            topValueDisplay =
                topValue.toLocaleString() +
                " hours";

        }

        else {

            topValueDisplay =
                "₹" +
                topValue.toLocaleString();

        }


        if (insightsElement) {

            insightsElement.innerHTML = `

                <div class="insight">
                    📊 <strong>KPI:</strong>
                    ${escapeHTML(
                        data.kpi || "N/A"
                    )}
                </div>

                <div class="insight">
                    🎯 <strong>Detected Driver:</strong>
                    ${escapeHTML(
                        data.driver || "N/A"
                    )}
                </div>

                <div class="insight">
                    🏆 <strong>Top Contributor:</strong>
                    ${escapeHTML(
                        data.topCategory || "N/A"
                    )}
                    —
                    ${topValueDisplay}
                </div>

                <div class="insight">
                    🧠 <strong>Intelligence Narrative:</strong>
                    ${escapeHTML(
                        aiNarrative
                    )}
                </div>

                <div class="insight">
                    💡 <strong>Recommended Action:</strong>
                    ${escapeHTML(
                        aiRecommendation
                    )}
                </div>

                <div class="insight">
                    📈 <strong>Confidence:</strong>
                    ${formatConfidence(
                        data.confidence
                    )}
                </div>

                <div class="insight">
                    🔬 <strong>Analytical Method:</strong>
                    ${escapeHTML(
                        data.analyticalMethod ||
                        "N/A"
                    )}
                </div>

                <div class="insight">
                    📁 <strong>File:</strong>
                    ${escapeHTML(
                        data.fileName ||
                        file.name
                    )}
                </div>

                <div class="insight">
                    📋 <strong>Data Quality:</strong>
                    ${Number(
                        data.dataQuality || 0
                    ).toFixed(1)}%
                </div>

            `;

        }


        status.textContent =
            "✅ " +
            file.name +
            " analyzed successfully.";

    }


    catch (error) {

        console.error(
            "File analysis error:",
            error
        );


        status.textContent =
            "❌ " +
            error.message;

    }


    finally {

        if (analyzeBtn) {

            analyzeBtn.disabled =
                false;

            analyzeBtn.textContent =
                "🤖 Analyze with AI";

        }

    }

}


// =====================================================
// MULTI-SOURCE ANALYSIS API
// =====================================================

async function analyzeMultiSource(
    salesFile,
    inventoryFile,
    marketingFile
) {

    if (!salesFile) {
        throw new Error(
            "Sales file is missing."
        );
    }

    if (!inventoryFile) {
        throw new Error(
            "Inventory file is missing."
        );
    }

    if (!marketingFile) {
        throw new Error(
            "Marketing file is missing."
        );
    }


    const formData =
        new FormData();


    formData.append(
        "sales",
        salesFile
    );

    formData.append(
        "inventory",
        inventoryFile
    );

    formData.append(
        "marketing",
        marketingFile
    );


    console.log(
        "Sending multi-source analysis..."
    );


    const response =
        await fetch(
            `${API}/analysis/multi-source`,
            {
                method: "POST",
                body: formData
            }
        );


    const responseText =
        await response.text();


    console.log(
        "Multi-source HTTP status:",
        response.status
    );


    console.log(
        "Multi-source response:",
        responseText
    );


    let data;


    try {

        data =
            JSON.parse(responseText);

    }

    catch (error) {

        throw new Error(
            responseText ||
            "Backend returned an invalid response."
        );

    }


    if (!response.ok) {

        throw new Error(
            data.error ||
            data.message ||
            "Multi-source analysis failed."
        );

    }


    console.log(
        "Multi-source analysis result:",
        data
    );


    return data;

}


// =====================================================
// MULTI-SOURCE RESULT DISPLAY
// =====================================================

function displayMultiSourceResult(data) {

    if (!data) {
        return;
    }


    console.log(
        "Rendering multi-source result:",
        data
    );


    // =================================================
    // UPDATE TOP KPI CARDS
    // =================================================

    const revenueElement =
        document.getElementById("revenue");

    const quantityElement =
        document.getElementById("quantity");

    const categoriesElement =
        document.getElementById("categories");


    const revenueLabel =
        document.getElementById("revenueLabel");

    const quantityLabel =
        document.getElementById("quantityLabel");

    const categoryLabel =
        document.getElementById("categoryLabel");

    const categoryTitle =
        document.getElementById("categoryTitle");


    // =================================================
    // TOP KPI 1 — REVENUE
    // =================================================

    if (revenueLabel) {

        revenueLabel.textContent =
            "💰 Total Revenue";

    }


    if (revenueElement) {

        revenueElement.textContent =
            "₹" +
            Number(
                data.totalRevenue || 0
            ).toLocaleString();

    }


    // =================================================
    // TOP KPI 2 — QUANTITY
    // =================================================

    if (quantityLabel) {

        quantityLabel.textContent =
            "📦 Total Quantity";

    }


    if (quantityElement) {

        quantityElement.textContent =
            Number(
                data.totalQuantity || 0
            ).toLocaleString();

    }


    // =================================================
    // TOP KPI 3 — INVENTORY
    // =================================================

    if (categoryLabel) {

        categoryLabel.textContent =
            "🏭 Total Inventory";

    }


    if (categoriesElement) {

        categoriesElement.textContent =
            Number(
                data.totalStock || 0
            ).toLocaleString();

    }


    // =================================================
    // MULTI-SOURCE KPI OVERVIEW
    // =================================================

    if (categoryTitle) {

        categoryTitle.textContent =
            "📊 Multi-Source KPI Overview";

    }


    const categoryContainer =
        document.getElementById(
            "categoryData"
        );


    if (categoryContainer) {

        categoryContainer.innerHTML = `

            <div class="insight">
                ✅ <strong>Sales, Inventory and Marketing data successfully analyzed.</strong>
            </div>

            <div class="insight">
                📊 Cross-source KPI movements, driver impact and evidence are available in the AI Insights section.
            </div>

        `;

    }


    // =================================================
    // AI INSIGHTS ELEMENT
    // =================================================

    const insightsElement =
        document.getElementById(
            "insights"
        );


    if (!insightsElement) {

        console.error(
            "AI Insights element not found."
        );

        return;

    }


    // =================================================
    // DRIVER MOVEMENT SIGNALS
    // =================================================

    let driverHTML =
        "";


    if (
        data.driverContributions &&
        typeof data.driverContributions === "object"
    ) {

        driverHTML += `
            <div class="insight">
                📊 <strong>Driver Movement Signals</strong>
            </div>
        `;


        Object.entries(
            data.driverContributions
        ).forEach(
            ([driver, value]) => {

                const numericValue =
                    Number(value);


                let formattedValue;


                // Percentage-based drivers
                if (
                    driver === "Sales Volume" ||
                    driver === "Average Price" ||
                    driver === "Inventory Availability" ||
                    driver === "Marketing Conversions"
                ) {

                    formattedValue =
                        (numericValue >= 0 ? "+" : "") +
                        numericValue.toFixed(2) +
                        "%";

                }


                // Stockout = hours
                else if (
                    driver === "Stockout Hours"
                ) {

                    formattedValue =
                        (numericValue >= 0 ? "+" : "") +
                        numericValue.toFixed(2) +
                        " hours";

                }


                else {

                    formattedValue =
                        numericValue.toFixed(2);

                }


                driverHTML += `
                    <div class="insight">
                        📌 <strong>
                            ${escapeHTML(driver)}
                        :</strong>
                        ${formattedValue}
                    </div>
                `;

            }
        );

    }


    // =================================================
    // DRIVER IMPACT RANKING
    // =================================================

    let impactHTML =
        "";


    if (
        data.driverImpactScores &&
        typeof data.driverImpactScores === "object"
    ) {

        impactHTML += `
            <div class="insight">
                🎯 <strong>Driver Impact Ranking</strong>
            </div>
        `;


        const impactEntries =
            Object.entries(
                data.driverImpactScores
            )
            .sort(
                ([, a], [, b]) =>
                    Number(b) - Number(a)
            );


        impactEntries.forEach(
            ([driver, score]) => {

                const numericScore =
                    Number(score);


                const level =
                    data.driverImpactLevels &&
                    data.driverImpactLevels[driver]
                        ? data.driverImpactLevels[driver]
                        : getImpactLevel(
                            numericScore
                        );


                let icon =
                    "🟢";


                if (
                    level === "High"
                ) {

                    icon =
                        "🔴";

                }

                else if (
                    level === "Medium"
                ) {

                    icon =
                        "🟠";

                }


                impactHTML += `
                    <div class="insight">
                        ${icon}
                        <strong>
                            ${escapeHTML(driver)}
                        :</strong>
                        ${numericScore.toFixed(2)}
                        / 100
                        — <strong>
                            ${escapeHTML(level)}
                        </strong>
                    </div>
                `;

            }
        );

    }


    // =================================================
    // EVIDENCE
    // =================================================

    let evidenceHTML =
        "";


    if (
        Array.isArray(data.evidence) &&
        data.evidence.length > 0
    ) {

        evidenceHTML =
            data.evidence
                .map(
                    (item) => {

                        return `
                            <div class="insight">
                                🔎 ${escapeHTML(item)}
                            </div>
                        `;

                    }
                )
                .join("");

    }

    else {

        evidenceHTML = `
            <div class="insight">
                🔎 No additional evidence available.
            </div>
        `;

    }


    // =================================================
    // MOVEMENT PERCENTAGE
    // =================================================

    const movementPercentage =
        Number(
            data.movementPercentage || 0
        );


    // =================================================
    // MAIN MULTI-SOURCE AI INSIGHTS
    // =================================================

    insightsElement.innerHTML = `

        <div class="insight">
            📊 <strong>KPI:</strong>
            ${escapeHTML(
                data.kpi || "Revenue"
            )}
        </div>


        <div class="insight">
            📉 <strong>Movement:</strong>
            ${escapeHTML(
                data.movement || "N/A"
            )}
            (${movementPercentage >= 0 ? "+" : ""}
            ${movementPercentage.toFixed(2)}%)
        </div>


        <div class="insight">
            🎯 <strong>Primary Driver:</strong>
            ${escapeHTML(
                data.primaryDriver || "N/A"
            )}
        </div>


        <div class="insight">
            🧠 <strong>Driver Explanation:</strong>
            ${escapeHTML(
                data.driverExplanation || "N/A"
            )}
        </div>


        ${driverHTML}


        ${impactHTML}


        <div class="insight">
            🔍 <strong>Evidence:</strong>
        </div>


        ${evidenceHTML}


        <div class="insight">
            📈 <strong>Confidence:</strong>
            ${formatConfidence(
                data.confidence
            )}
        </div>


        <div class="insight">
            🔬 <strong>Analytical Method:</strong>
            ${escapeHTML(
                data.analyticalMethod ||
                "N/A"
            )}
        </div>


        <div class="insight">
            💡 <strong>Recommended Action:</strong>
            ${escapeHTML(
                data.recommendation ||
                "N/A"
            )}
        </div>


        <div class="insight">
            👤 <strong>Owner:</strong>
            ${escapeHTML(
                data.owner ||
                "N/A"
            )}
        </div>


        <div class="insight">
            📡 <strong>Monitoring Plan:</strong>
            ${escapeHTML(
                data.monitoringPlan ||
                "N/A"
            )}
        </div>


        <div class="insight">
            💰 <strong>Total Revenue:</strong>
            ₹${Number(
                data.totalRevenue || 0
            ).toLocaleString()}
        </div>


        <div class="insight">
            📦 <strong>Total Quantity:</strong>
            ${Number(
                data.totalQuantity || 0
            ).toLocaleString()}
        </div>


        <div class="insight">
            🏭 <strong>Total Inventory:</strong>
            ${Number(
                data.totalStock || 0
            ).toLocaleString()}
        </div>


        <div class="insight">
            ⏱️ <strong>Total Stockout Hours:</strong>
            ${Number(
                data.totalStockoutHours || 0
            ).toLocaleString()}
        </div>


        <div class="insight">
            📣 <strong>Marketing Spend:</strong>
            ₹${Number(
                data.totalMarketingSpend || 0
            ).toLocaleString()}
        </div>


        <div class="insight">
            ✅ <strong>Marketing Conversions:</strong>
            ${Number(
                data.totalConversions || 0
            ).toLocaleString()}
        </div>

    `;

}


// =====================================================
// MULTI-SOURCE BUTTON
// =====================================================

async function runMultiSourceAnalysis() {

    const salesInput =
        document.getElementById("salesFile");

    const inventoryInput =
        document.getElementById("inventoryFile");

    const marketingInput =
        document.getElementById("marketingFile");

    const button =
        document.getElementById("multiSourceBtn");

    const status =
        document.getElementById("multiSourceStatus");


    const salesFile =
        salesInput?.files[0];

    const inventoryFile =
        inventoryInput?.files[0];

    const marketingFile =
        marketingInput?.files[0];


    if (!salesFile) {

        status.textContent =
            "❌ Please select sales.csv.";

        return;

    }


    if (!inventoryFile) {

        status.textContent =
            "❌ Please select inventory.csv.";

        return;

    }


    if (!marketingFile) {

        status.textContent =
            "❌ Please select marketing.csv.";

        return;

    }


    if (button) {

        button.disabled =
            true;

        button.textContent =
            "⏳ Analyzing...";

    }


    if (status) {

        status.textContent =
            "⏳ Connecting Sales + Inventory + Marketing...";

    }


    try {

        const data =
            await analyzeMultiSource(
                salesFile,
                inventoryFile,
                marketingFile
            );


        // IMPORTANT:
        // Correct function name
        displayMultiSourceResult(
            data
        );


        if (status) {

            status.textContent =
                "✅ Multi-source analysis completed successfully.";

        }

    }


    catch (error) {

        console.error(
            "Multi-source error:",
            error
        );


        if (status) {

            status.textContent =
                "❌ " +
                error.message;

        }

    }


    finally {

        if (button) {

            button.disabled =
                false;

            button.textContent =
                "🔍 Analyze All Sources";

        }

    }

}


// =====================================================
// OPTIONAL HELPER
// =====================================================

async function runMultiSourceFromInputs() {

    const salesInput =
        document.getElementById(
            "salesFile"
        );

    const inventoryInput =
        document.getElementById(
            "inventoryFile"
        );

    const marketingInput =
        document.getElementById(
            "marketingFile"
        );


    const salesFile =
        salesInput?.files[0];

    const inventoryFile =
        inventoryInput?.files[0];

    const marketingFile =
        marketingInput?.files[0];


    const status =
        document.getElementById(
            "uploadStatus"
        );


    try {

        if (status) {

            status.textContent =
                "⏳ Analyzing sales + inventory + marketing...";

        }


        const data =
            await analyzeMultiSource(
                salesFile,
                inventoryFile,
                marketingFile
            );


        displayMultiSourceResult(
            data
        );


        if (status) {

            status.textContent =
                "✅ Multi-source business analysis completed successfully.";

        }

    }


    catch (error) {

        console.error(
            "Multi-source execution error:",
            error
        );


        if (status) {

            status.textContent =
                "❌ " +
                error.message;

        }

    }

}


// =====================================================
// IMPACT LEVEL
// =====================================================

function getImpactLevel(score) {

    const value =
        Number(score);


    if (value >= 70) {

        return "High";

    }


    if (value >= 40) {

        return "Medium";

    }


    return "Low";

}


// =====================================================
// CONFIDENCE FORMATTER
// =====================================================

function formatConfidence(confidence) {

    const value =
        Number(confidence);


    if (
        Number.isNaN(value)
    ) {

        return "0%";

    }


    if (value <= 1) {

        return Math.round(
            value * 100
        ) + "%";

    }


    return Math.round(
        value
    ) + "%";

}


// =====================================================
// SECURITY HELPER
// =====================================================

function escapeHTML(value) {

    const div =
        document.createElement(
            "div"
        );


    div.textContent =
        String(
            value ?? ""
        );


    return div.innerHTML;

}