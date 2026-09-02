
const API = "http://localhost:8080/api";


// =====================================================
// AUTHENTICATION STATE
// =====================================================

let authState = {
    authenticated: false,
    username: null,
    password: null,
    role: null,
    persona: null
};


// =====================================================
// DEMO USERS
// =====================================================

const DEMO_USERS = {

    supply: {
        role: "SUPPLY_CHAIN_MANAGER",
        persona: "Supply Chain Manager"
    },

    marketing: {
        role: "MARKETING_MANAGER",
        persona: "Marketing Manager"
    },

    executive: {
        role: "EXECUTIVE",
        persona: "Executive"
    }

};


// =====================================================
// PAGE LOAD
// =====================================================

document.addEventListener("DOMContentLoaded", () => {

    const analyzeBtn =
        document.getElementById("analyzeBtn");

    const fileInput =
        document.getElementById("csvFile");


    if (analyzeBtn) {

        analyzeBtn.addEventListener(
            "click",
            analyzeCSV
        );

    }


    if (fileInput) {

        fileInput.addEventListener("change", () => {

            const status =
                document.getElementById("uploadStatus");


            if (
                fileInput.files &&
                fileInput.files.length > 0 &&
                status
            ) {

                status.textContent =
                    "📁 Selected: " +
                    fileInput.files[0].name;

            }

        });

    }


    // =================================================
    // PERSONA CHANGE
    // =================================================

    const personaSelect =
        document.getElementById("personaSelect");


    if (personaSelect) {

        personaSelect.addEventListener("change", () => {

            console.log(
                "Authorized persona:",
                personaSelect.value
            );

        });

    }


    // =================================================
    // INITIAL UI STATE
    // =================================================

    resetAuthenticatedUI();

});


// =====================================================
// LOGIN
// =====================================================

function loginUser() {

    const usernameInput =
        document.getElementById("username");

    const passwordInput =
        document.getElementById("password");


    const username =
        usernameInput?.value
            ?.trim()
            .toLowerCase();


    const password =
        passwordInput?.value || "";


    if (!username || !password) {

        showLoginMessage(
            "Please enter username and password.",
            "error"
        );

        return;

    }


    const user =
        DEMO_USERS[username];


    if (!user) {

        showLoginMessage(
            "Invalid username.",
            "error"
        );

        return;

    }


    /*
     * IMPORTANT:
     *
     * Frontend does NOT decide whether the password
     * is correct.
     *
     * The actual authentication authority is
     * Spring Security on the backend.
     *
     * We temporarily store the credentials so that
     * Basic Authentication can be sent with API calls.
     */


    authState = {

        authenticated: true,

        username: username,

        password: password,

        role: user.role,

        persona: user.persona

    };


    updateAuthenticatedUI();


    showLoginMessage(
        "Credentials loaded. Verifying with backend...",
        "success"
    );


    /*
     * Verify credentials immediately.
     *
     * This prevents the UI from saying "logged in"
     * when the backend password is actually wrong.
     */

    verifyBackendAuthentication();

}


// =====================================================
// VERIFY BACKEND AUTHENTICATION
// =====================================================

async function verifyBackendAuthentication() {

    try {

        const response =
            await authenticatedFetch(
                `${API}/health`,
                {
                    method: "GET"
                }
            );


        /*
         * If health endpoint is public, this alone
         * cannot verify credentials.
         *
         * Therefore we do not treat this request as
         * the final authorization decision.
         */


        if (response.status === 401) {

            handleUnauthorized();

            showLoginMessage(
                "❌ Invalid username or password.",
                "error"
            );

            return;

        }


        /*
         * Authentication will be definitively checked
         * when the protected analysis endpoint is called.
         */

        showLoginMessage(
            "✅ Ready. Backend authorization will be enforced during analysis.",
            "success"
        );

    }


    catch (error) {

        console.error(
            "Authentication verification error:",
            error
        );

        /*
         * Do not immediately logout on network errors.
         * The backend may simply not be running.
         */

        showLoginMessage(
            "⚠️ Backend connection could not be verified. Start Spring Boot before analysis.",
            "error"
        );

    }

}


// =====================================================
// UPDATE AUTHENTICATED UI
// =====================================================

function updateAuthenticatedUI() {

    const authSection =
        document.getElementById("authSection");

    const userSession =
        document.getElementById("userSession");

    const loggedInUser =
        document.getElementById("loggedInUser");

    const loggedInRole =
        document.getElementById("loggedInRole");

    const personaSelect =
        document.getElementById("personaSelect");


    if (authSection) {

        authSection.style.display =
            "none";

    }


    if (userSession) {

        userSession.style.display =
            "flex";

    }


    if (loggedInUser) {

        loggedInUser.textContent =
            "👤 " + authState.username;

    }


    if (loggedInRole) {

        loggedInRole.textContent =
            authState.persona;

    }


    if (personaSelect) {

        personaSelect.innerHTML = "";


        const option =
            document.createElement("option");


        option.value =
            authState.persona;


        option.textContent =
            authState.persona;


        option.selected =
            true;


        personaSelect.appendChild(
            option
        );


        /*
         * Persona is locked.
         *
         * User cannot select another persona.
         *
         * Backend also independently validates
         * role/persona authorization.
         */

        personaSelect.disabled =
            true;

    }


    const multiSourceBtn =
        document.getElementById(
            "multiSourceBtn"
        );


    if (multiSourceBtn) {

        multiSourceBtn.disabled =
            false;

    }


    const status =
        document.getElementById(
            "multiSourceStatus"
        );


    if (status) {

        status.textContent =
            "Select all 3 files to begin.";

    }

}


// =====================================================
// RESET AUTH UI
// =====================================================

function resetAuthenticatedUI() {

    const authSection =
        document.getElementById("authSection");

    const userSession =
        document.getElementById("userSession");

    const personaSelect =
        document.getElementById("personaSelect");


    if (authSection) {

        authSection.style.display =
            "block";

    }


    if (userSession) {

        userSession.style.display =
            "none";

    }


    if (personaSelect) {

        personaSelect.innerHTML =
            `<option value="">🔐 Login required</option>`;

        personaSelect.disabled =
            true;

    }


    const multiSourceBtn =
        document.getElementById(
            "multiSourceBtn"
        );


    if (multiSourceBtn) {

        multiSourceBtn.disabled =
            false;

    }

}


// =====================================================
// LOGOUT
// =====================================================

function logoutUser() {

    authState = {

        authenticated: false,

        username: null,

        password: null,

        role: null,

        persona: null

    };


    resetAuthenticatedUI();


    const usernameInput =
        document.getElementById("username");

    const passwordInput =
        document.getElementById("password");


    if (usernameInput) {

        usernameInput.value = "";

    }


    if (passwordInput) {

        passwordInput.value = "";

    }


    const loginMessage =
        document.getElementById(
            "loginMessage"
        );


    if (loginMessage) {

        loginMessage.textContent =
            "Logged out successfully.";

        loginMessage.className =
            "login-message success";

    }


    const status =
        document.getElementById(
            "multiSourceStatus"
        );


    if (status) {

        status.textContent =
            "🔐 Login required.";

    }


    const insights =
        document.getElementById(
            "insights"
        );


    if (insights) {

        insights.innerHTML = `

            <div class="insight">

                🔐 Please login to generate
                business intelligence.

            </div>

        `;

    }

}


// =====================================================
// LOGIN MESSAGE
// =====================================================

function showLoginMessage(
    message,
    type
) {

    const element =
        document.getElementById(
            "loginMessage"
        );


    if (!element) {

        return;

    }


    element.textContent =
        message;


    element.className =
        `login-message ${type}`;

}


// =====================================================
// BASIC AUTH HEADER
// =====================================================

function getAuthorizationHeader() {

    if (
        !authState.username ||
        !authState.password
    ) {

        return null;

    }


    const credentials =
        btoa(
            `${authState.username}:${authState.password}`
        );


    return `Basic ${credentials}`;

}


// =====================================================
// AUTHENTICATED FETCH
// =====================================================

async function authenticatedFetch(
    url,
    options = {}
) {

    if (!authState.authenticated) {

        throw new Error(
            "Authentication required."
        );

    }


    const headers = {

        ...(options.headers || {}),

        "Authorization":
            getAuthorizationHeader()

    };


    return fetch(
        url,
        {
            ...options,
            headers
        }
    );

}


// =====================================================
// UNAUTHORIZED HANDLER
// =====================================================

function handleUnauthorized() {

    alert(
        "Authentication failed. Please login again."
    );


    logoutUser();

}


// =====================================================
// FORBIDDEN HANDLER
// =====================================================

async function handleForbidden(
    response
) {

    let errorData = {};


    try {

        errorData =
            await response.json();

    }

    catch (ignored) {

        // Backend may return non-JSON response.

    }


    alert(
        errorData.message ||
        "You are not authorized for this decision persona."
    );

}


// =====================================================
// MAIN SINGLE-FILE ANALYSIS
// =====================================================

async function analyzeCSV() {

    /*
     * Single-file API is also protected by
     * Spring Security.
     */

    if (!authState.authenticated) {

        alert(
            "Please login before running analysis."
        );

        return;

    }


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

        if (status) {

            status.textContent =
                "❌ Please select a CSV or Excel file first.";

        }

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

        if (status) {

            status.textContent =
                "❌ Please upload a CSV, XLSX or XLS file.";

        }

        return;

    }


    const formData =
        new FormData();


    formData.append(
        "file",
        file
    );


    if (status) {

        status.textContent =
            "⏳ Analyzing " +
            file.name +
            "...";

    }


    if (analyzeBtn) {

        analyzeBtn.disabled =
            true;

        analyzeBtn.textContent =
            "⏳ Analyzing...";

    }


    try {

        const response =
            await authenticatedFetch(
                `${API}/csv/upload`,
                {
                    method: "POST",
                    body: formData
                }
            );


        if (response.status === 401) {

            handleUnauthorized();

            return;

        }


        if (response.status === 403) {

            await handleForbidden(
                response
            );

            return;

        }


        const responseText =
            await response.text();


        let data;


        try {

            data =
                JSON.parse(
                    responseText
                );

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
            document.getElementById(
                "revenue"
            );

        const quantityElement =
            document.getElementById(
                "quantity"
            );

        const categoriesElement =
            document.getElementById(
                "categories"
            );

        const revenueLabel =
            document.getElementById(
                "revenueLabel"
            );

        const quantityLabel =
            document.getElementById(
                "quantityLabel"
            );

        const categoryLabel =
            document.getElementById(
                "categoryLabel"
            );

        const categoryTitle =
            document.getElementById(
                "categoryTitle"
            );


        // =================================================
        // INVENTORY
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


        // =================================================
        // QUANTITY
        // =================================================

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


        // =================================================
        // COMPLAINTS
        // =================================================

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


        // =================================================
        // STOCKOUT
        // =================================================

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


        // =================================================
        // REVENUE DEFAULT
        // =================================================

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
        // CATEGORY PERFORMANCE
        // =================================================

        const categoryContainer =
            document.getElementById(
                "categoryData"
            );


        if (categoryContainer) {

            categoryContainer.innerHTML =
                "";


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
        // AI INSIGHTS
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


        if (status) {

            status.textContent =
                "✅ " +
                file.name +
                " analyzed successfully.";

        }

    }


    catch (error) {

        console.error(
            "File analysis error:",
            error
        );


        if (status) {

            status.textContent =
                "❌ " +
                error.message;

        }

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
    marketingFile,
    persona
) {

    // =================================================
    // AUTHENTICATION CHECK
    // =================================================

    if (!authState.authenticated) {

        throw new Error(
            "Please login before analysis."
        );

    }


    // =================================================
    // FILE VALIDATION
    // =================================================

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


    // =================================================
    // AUTHORIZED PERSONA
    // =================================================

    /*
     * Persona is taken from authenticated state.
     *
     * Do NOT trust a freely editable UI persona.
     */

    const authorizedPersona =
        authState.persona;


    if (!authorizedPersona) {

        throw new Error(
            "No authorized decision persona found."
        );

    }


    // =================================================
    // FORM DATA
    // =================================================

    const formData =
        new FormData();


    formData.append(
        "salesFile",
        salesFile
    );


    formData.append(
        "inventoryFile",
        inventoryFile
    );


    formData.append(
        "marketingFile",
        marketingFile
    );


    /*
     * Backend receives the persona.
     *
     * Backend should ALSO validate that the persona
     * matches the authenticated Spring Security role.
     */

    formData.append(
        "persona",
        authorizedPersona
    );


    // =================================================
    // DEBUG
    // =================================================

    console.log(
        "Sending multi-source analysis..."
    );


    console.log(
        "Authenticated user:",
        authState.username
    );


    console.log(
        "Authenticated role:",
        authState.role
    );


    console.log(
        "Authorized persona:",
        authorizedPersona
    );


    // =================================================
    // API REQUEST
    // =================================================

    const response =
        await authenticatedFetch(
            `${API}/analysis/multi-source`,
            {
                method: "POST",

                /*
                 * DO NOT manually set Content-Type.
                 *
                 * FormData automatically creates the
                 * correct multipart/form-data boundary.
                 */

                body: formData
            }
        );


    console.log(
        "Multi-source HTTP status:",
        response.status
    );


    // =================================================
    // 401 UNAUTHORIZED
    // =================================================

    if (response.status === 401) {

        handleUnauthorized();

        throw new Error(
            "Authentication failed."
        );

    }


    // =================================================
    // 403 FORBIDDEN
    // =================================================

    if (response.status === 403) {

        await handleForbidden(
            response
        );

        throw new Error(
            "You are not authorized for this decision persona."
        );

    }


    // =================================================
    // READ RESPONSE
    // =================================================

    const responseText =
        await response.text();


    console.log(
        "Multi-source response:",
        responseText
    );


    let data;


    // =================================================
    // PARSE JSON
    // =================================================

    try {

        data =
            JSON.parse(
                responseText
            );

    }

    catch (error) {

        console.error(
            "Invalid backend response:",
            error
        );

        throw new Error(
            responseText ||
            "Backend returned an invalid response."
        );

    }


    // =================================================
    // RESPONSE VALIDATION
    // =================================================

    if (!response.ok) {

        throw new Error(
            data.error ||
            data.message ||
            "Multi-source analysis failed."
        );

    }


    // =================================================
    // SAVE LAST ANALYSIS
    // =================================================

    console.log(
        "Multi-source analysis result:",
        data
    );


    window.lastAnalysisResult =
        data;


    // =================================================
    // ABSTENTION UI
    // =================================================

    renderAbstentionState(
        data
    );


    // =================================================
    // FEEDBACK UI
    // =================================================

    const feedbackSection =
        document.getElementById(
            "feedbackSection"
        );


    if (feedbackSection) {

        feedbackSection.style.display =
            "block";

    }


    // =================================================
    // DEBUG TOTALS
    // =================================================

    console.log(
        "TOTAL REVENUE:",
        data.totalRevenue
    );


    console.log(
        "TOTAL QUANTITY:",
        data.totalQuantity
    );


    console.log(
        "TOTAL INVENTORY:",
        data.totalInventory
    );


    console.log(
        "TOTAL STOCKOUT HOURS:",
        data.totalStockoutHours
    );


    console.log(
        "MARKETING SPEND:",
        data.marketingSpend
    );


    console.log(
        "MARKETING CONVERSIONS:",
        data.marketingConversions
    );


    console.log(
        "CONFIDENCE:",
        data.confidence
    );


    console.log(
        "ABSTAINED:",
        data.abstained
    );


    // =================================================
    // RETURN
    // =================================================

    return data;

}


// =====================================================
// MULTI-SOURCE BUTTON
// =====================================================

async function runMultiSourceAnalysis() {

    // =================================================
    // AUTHENTICATION CHECK
    // =================================================

    if (!authState.authenticated) {

        alert(
            "Please login before running analysis."
        );

        return;

    }


    if (!authState.persona) {

        alert(
            "No authorized decision persona found."
        );

        return;

    }


    // =================================================
    // INPUT ELEMENTS
    // =================================================

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


    const personaSelect =
        document.getElementById(
            "personaSelect"
        );


    const button =
        document.getElementById(
            "multiSourceBtn"
        );


    const status =
        document.getElementById(
            "multiSourceStatus"
        );


    // =================================================
    // FILES
    // =================================================

    const salesFile =
        salesInput?.files[0];


    const inventoryFile =
        inventoryInput?.files[0];


    const marketingFile =
        marketingInput?.files[0];


    /*
     * Do NOT trust personaSelect.value.
     *
     * Persona comes from authenticated state.
     */

    const persona =
        authState.persona;


    // =================================================
    // FILE VALIDATION
    // =================================================

    if (!salesFile) {

        if (status) {

            status.textContent =
                "❌ Please select sales.csv.";

        }

        return;

    }


    if (!inventoryFile) {

        if (status) {

            status.textContent =
                "❌ Please select inventory.csv.";

        }

        return;

    }


    if (!marketingFile) {

        if (status) {

            status.textContent =
                "❌ Please select marketing.csv.";

        }

        return;

    }


    // =================================================
    // LOADING STATE
    // =================================================

    if (button) {

        button.disabled =
            true;

        button.textContent =
            "⏳ Analyzing...";

    }


    if (status) {

        status.textContent =
            "⏳ Analyzing for " +
            persona +
            "...";

    }


    // =================================================
    // RUN ANALYSIS
    // =================================================

    try {

        const data =
            await analyzeMultiSource(
                salesFile,
                inventoryFile,
                marketingFile,
                persona
            );


        // =================================================
        // DISPLAY RESULT
        // =================================================

        displayMultiSourceResult(
            data
        );


        // =================================================
        // STATUS
        // =================================================

        if (status) {

            if (
                data &&
                data.abstained === true
            ) {

                status.textContent =
                    "⚠️ Analysis completed with abstention for " +
                    persona +
                    ".";

            }

            else {

                status.textContent =
                    "✅ Analysis completed for " +
                    persona +
                    ".";

            }

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
                (
                    error.message ||
                    "Analysis failed."
                );

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
// MULTI-SOURCE RESULT DISPLAY
// =====================================================

function displayMultiSourceResult(
    data
) {

    if (!data) {

        console.error(
            "No multi-source result received."
        );

        return;

    }


    console.log(
        "Rendering multi-source result:",
        data
    );


    // =================================================
    // KPI ELEMENTS
    // =================================================

    const revenueElement =
        document.getElementById(
            "revenue"
        );


    const quantityElement =
        document.getElementById(
            "quantity"
        );


    const categoriesElement =
        document.getElementById(
            "categories"
        );


    const revenueLabel =
        document.getElementById(
            "revenueLabel"
        );


    const quantityLabel =
        document.getElementById(
            "quantityLabel"
        );


    const categoryLabel =
        document.getElementById(
            "categoryLabel"
        );


    const categoryTitle =
        document.getElementById(
            "categoryTitle"
        );


    // =================================================
    // REVENUE
    // =================================================

    if (revenueLabel) {

        revenueLabel.textContent =
            "💰 Total Revenue";

    }


    if (revenueElement) {

        revenueElement.textContent =
            "₹" +
            Number(
                data.totalRevenue ?? 0
            ).toLocaleString();

    }


    // =================================================
    // QUANTITY
    // =================================================

    if (quantityLabel) {

        quantityLabel.textContent =
            "📦 Total Quantity";

    }


    if (quantityElement) {

        quantityElement.textContent =
            Number(
                data.totalQuantity ?? 0
            ).toLocaleString();

    }


    // =================================================
    // INVENTORY
    // =================================================

    if (categoryLabel) {

        categoryLabel.textContent =
            "🏭 Inventory Stock Units";

    }


    if (categoriesElement) {

        categoriesElement.textContent =
            Number(
                data.totalInventory ?? 0
            ).toLocaleString();

    }


    // =================================================
    // CATEGORY TITLE
    // =================================================

    if (categoryTitle) {

        categoryTitle.textContent =
            "📊 Multi-Source KPI Overview";

    }


    // =================================================
    // CATEGORY CONTAINER
    // =================================================

    const categoryContainer =
        document.getElementById(
            "categoryData"
        );


    if (categoryContainer) {

        categoryContainer.innerHTML = `

            <div class="insight">

                ✅ <strong>
                    Sales, Inventory and Marketing
                    data successfully analyzed.
                </strong>

            </div>


            <div class="insight">

                📊 Cross-source KPI movements,
                driver impact and evidence are
                available in the AI Insights section.

            </div>


            <div class="insight">

                ⏱️ <strong>
                    Total Stockout Hours:
                </strong>

                ${Number(
                    data.totalStockoutHours ?? 0
                ).toLocaleString()}

            </div>


            <div class="insight">

                📣 <strong>
                    Marketing Spend:
                </strong>

                ₹${Number(
                    data.marketingSpend ?? 0
                ).toLocaleString()}

            </div>


            <div class="insight">

                ✅ <strong>
                    Marketing Conversions:
                </strong>

                ${Number(
                    data.marketingConversions ?? 0
                ).toLocaleString()}

            </div>

        `;

    }


    // =================================================
    // INSIGHTS ELEMENT
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
    // PERSONA
    // =================================================

    const persona =
        data.persona ||
        authState.persona ||
        "Unknown";


    // =================================================
    // DRIVER MOVEMENTS
    // =================================================

    let driverHTML =
        "";


    const driverMovements =
        data.driverMovements ||
        data.driverContributions ||
        {};


    if (
        driverMovements &&
        typeof driverMovements === "object"
    ) {

        driverHTML += `

            <div class="insight">

                📊 <strong>
                    Driver Movement Signals
                </strong>

            </div>

        `;


        Object.entries(
            driverMovements
        ).forEach(
            ([driver, value]) => {

                const numericValue =
                    Number(value);


                const formattedValue =
                    (
                        numericValue >= 0
                            ? "+"
                            : ""
                    ) +
                    numericValue.toFixed(2) +
                    "%";


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

                🎯 <strong>
                    Driver Impact Ranking
                </strong>

            </div>

        `;


        const impactEntries =
            Object.entries(
                data.driverImpactScores
            )
            .sort(
                ([, a], [, b]) =>
                    Number(b) -
                    Number(a)
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

                        —

                        <strong>
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
                    (item) => `

                        <div class="insight">

                            🔎
                            ${escapeHTML(item)}

                        </div>

                    `
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
    // MOVEMENT
    // =================================================

    const movementPercentage =
        Number(
            data.movementPercentage ?? 0
        );


    // =================================================
    // ABSTENTION
    // =================================================

    let abstentionHTML =
        "";


    if (
        data.abstained === true
    ) {

        abstentionHTML = `

            <div class="insight">

                ⚠️ <strong>
                    Automated Decision Abstained
                </strong>

                <br><br>

                The system did not issue a
                recommendation because the
                available evidence was insufficient
                or contradictory.

                <br><br>

                <strong>
                    Clarification:
                </strong>

                ${escapeHTML(
                    data.clarificationRequest ||
                    "Additional evidence is required before a reliable decision can be made."
                )}

            </div>

        `;

    }


    // =================================================
    // PERSONA NARRATIVE
    // =================================================

    const personaNarrative =
        data.personaNarrative ||
        "No persona-specific narrative available.";


    // =================================================
    // PERSONA ACTION
    // =================================================

    const personaAction =
        data.personaAction ||
        data.recommendation ||
        "No recommendation available.";


    // =================================================
    // MAIN INSIGHTS
    // =================================================

    insightsElement.innerHTML = `

        <div class="insight">

            🔐 <strong>
                Authenticated User:
            </strong>

            ${escapeHTML(
                authState.username || "N/A"
            )}

        </div>


        <div class="insight">

            👤 <strong>
                Authorized Persona:
            </strong>

            ${escapeHTML(
                persona
            )}

        </div>


        <div class="insight">

            📊 <strong>
                KPI:
            </strong>

            ${escapeHTML(
                data.kpi || "Revenue"
            )}

        </div>


        <div class="insight">

            📉 <strong>
                Movement:
            </strong>

            ${escapeHTML(
                data.movement || "N/A"
            )}

            (

            ${movementPercentage >= 0 ? "+" : ""}

            ${movementPercentage.toFixed(2)}%

            )

        </div>


        <div class="insight">

            🎯 <strong>
                Primary Driver:
            </strong>

            ${escapeHTML(
                data.primaryDriver || "N/A"
            )}

        </div>


        <div class="insight">

            🧠 <strong>
                Driver Explanation:
            </strong>

            ${escapeHTML(
                data.driverExplanation || "N/A"
            )}

        </div>


        ${driverHTML}


        ${impactHTML}


        <div class="insight">

            🔍 <strong>
                Evidence
            </strong>

        </div>


        ${evidenceHTML}


        <div class="insight">

            📈 <strong>
                Confidence:
            </strong>

            ${formatConfidence(
                data.confidence
            )}

        </div>


        <div class="insight">

            🔬 <strong>
                Analytical Method:
            </strong>

            ${escapeHTML(
                data.analyticalMethod ||
                "N/A"
            )}

        </div>


        ${abstentionHTML}


        <div class="insight">

            🧠 <strong>
                Persona Narrative:
            </strong>

            ${escapeHTML(
                personaNarrative
            )}

        </div>


        <div class="insight">

            💡 <strong>
                Persona Action:
            </strong>

            ${escapeHTML(
                personaAction
            )}

        </div>


        <div class="insight">

            👤 <strong>
                Owner:
            </strong>

            ${escapeHTML(
                data.owner ||
                "N/A"
            )}

        </div>


        <div class="insight">

            📡 <strong>
                Monitoring Plan:
            </strong>

            ${escapeHTML(
                data.monitoringPlan ||
                "N/A"
            )}

        </div>


        <!-- =========================================
             BACKEND VERIFIED TOTALS
             ========================================= -->

        <div class="insight">

            💰 <strong>
                Total Revenue:
            </strong>

            ₹${Number(
                data.totalRevenue ?? 0
            ).toLocaleString()}

        </div>


        <div class="insight">

            📦 <strong>
                Total Quantity:
            </strong>

            ${Number(
                data.totalQuantity ?? 0
            ).toLocaleString()}

        </div>


        <div class="insight">

            🏭 <strong>
                Inventory Stock Units:
            </strong>

            ${Number(
                data.totalInventory ?? 0
            ).toLocaleString()}

        </div>


        <div class="insight">

            ⏱️ <strong>
                Total Stockout Hours:
            </strong>

            ${Number(
                data.totalStockoutHours ?? 0
            ).toLocaleString()}

        </div>


        <div class="insight">

            📣 <strong>
                Marketing Spend:
            </strong>

            ₹${Number(
                data.marketingSpend ?? 0
            ).toLocaleString()}

        </div>


        <div class="insight">

            ✅ <strong>
                Marketing Conversions:
            </strong>

            ${Number(
                data.marketingConversions ?? 0
            ).toLocaleString()}

        </div>


        <!-- =========================================
             DATA ALIGNMENT
             ========================================= -->

        <div class="insight">

            📅 <strong>
                Common Aligned Dates:
            </strong>

            ${Number(
                data.commonDateCount ?? 0
            ).toLocaleString()}

        </div>


        <div class="insight">

            🧮 <strong>
                Sparse History:
            </strong>

            ${
                data.sparseHistory === true
                    ? "⚠️ Yes"
                    : "✅ No"
            }

        </div>

    `;

}


// =====================================================
// IMPACT LEVEL
// =====================================================

function getImpactLevel(
    score
) {

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

function formatConfidence(
    confidence
) {

    const value =
        Number(confidence);


    if (Number.isNaN(value)) {

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
// XSS SAFE HTML
// =====================================================

function escapeHTML(
    value
) {

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


// =====================================================
// ABSTENTION UI
// =====================================================

function renderAbstentionState(
    data
) {

    /*
     * Your HTML currently uses:
     *
     *     id="insights"
     *
     * So this function intentionally uses
     * "insights" as well.
     */

    const insightSection =
        document.getElementById(
            "insights"
        );


    if (!insightSection) {

        console.error(
            "AI Insights element not found."
        );

        return;

    }


    // =================================================
    // NORMAL ANALYSIS
    // =================================================

    if (
        !data ||
        data.abstained !== true
    ) {

        insightSection.style.display =
            "block";

        return;

    }


    // =================================================
    // ABSTENTION
    // =================================================

    insightSection.innerHTML = `

        <div class="abstention-card">

            <div class="abstention-icon">
                🛑
            </div>


            <div>

                <h3>
                    Insufficient Evidence —
                    No Recommendation Issued
                </h3>


                <p>

                    DecisionLens could not establish
                    enough reliable evidence to produce
                    a business recommendation.

                </p>


                <div class="abstention-reason">

                    <strong>
                        Why?
                    </strong>

                    ${
                        escapeHTML(
                            data.clarificationRequest ||
                            "More aligned historical data is required."
                        )
                    }

                </div>


                <div class="abstention-rule">

                    🔍 Confidence gating is active.
                    The system abstains instead of
                    inventing a conclusion.

                </div>

            </div>

        </div>

    `;


    insightSection.style.display =
        "block";

}


// =====================================================
// OPTIONAL MULTI-SOURCE HELPER
// =====================================================

async function runMultiSourceFromInputs() {

    if (!authState.authenticated) {

        alert(
            "Please login before running analysis."
        );

        return;

    }


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
            "multiSourceStatus"
        );


    try {

        if (status) {

            status.textContent =
                "⏳ Analyzing for " +
                (
                    authState.persona ||
                    "authorized persona"
                ) +
                "...";

        }


        const data =
            await analyzeMultiSource(
                salesFile,
                inventoryFile,
                marketingFile,
                authState.persona
            );


        displayMultiSourceResult(
            data
        );


        if (status) {

            status.textContent =
                data.abstained === true
                    ? "⚠️ Analysis completed with abstention."
                    : "✅ Multi-source analysis completed for " +
                      (
                          authState.persona ||
                          "authorized persona"
                      ) +
                      ".";

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
                (
                    error.message ||
                    "Analysis failed."
                );

        }

    }

}


// =====================================================
// FEEDBACK API
// =====================================================

async function submitFeedback(
    feedbackType
) {

    // =================================================
    // AUTHENTICATION
    // =================================================

    if (!authState.authenticated) {

        alert(
            "Please login first."
        );

        return;

    }


    // =================================================
    // ELEMENTS
    // =================================================

    const feedbackMessage =
        document.getElementById(
            "feedbackMessage"
        );


    const commentElement =
        document.getElementById(
            "feedbackComment"
        );


    const comment =
        commentElement?.value ||
        "";


    // =================================================
    // CREDENTIALS
    // =================================================

    const credentials =
        btoa(
            `${authState.username}:${authState.password}`
        );


    // =================================================
    // LAST ANALYSIS
    // =================================================

    const currentAnalysis =
        window.lastAnalysisResult ||
        {};


    // =================================================
    // SEND FEEDBACK
    // =================================================

    try {

        if (feedbackMessage) {

            feedbackMessage.textContent =
                "Saving feedback...";

            feedbackMessage.style.color =
                "#374151";

        }


        const response =
            await fetch(
                `${API}/feedback`,
                {
                    method: "POST",

                    headers: {

                        "Authorization":
                            `Basic ${credentials}`,

                        "Content-Type":
                            "application/json"

                    },

                    body: JSON.stringify({

                        feedbackType:
                            feedbackType,

                        comment:
                            comment,

                        kpi:
                            currentAnalysis.kpi ||
                            "Revenue",

                        primaryDriver:
                            currentAnalysis.primaryDriver ||
                            "",

                        confidence:
                            currentAnalysis.confidence ??
                            null,

                        abstained:
                            currentAnalysis.abstained ??
                            false

                    })

                }
            );


        // =================================================
        // 401
        // =================================================

        if (
            response.status === 401
        ) {

            logoutUser();

            throw new Error(
                "Authentication expired. Please login again."
            );

        }


        // =================================================
        // 403
        // =================================================

        if (
            response.status === 403
        ) {

            throw new Error(
                "You are not authorized to submit feedback."
            );

        }


        // =================================================
        // READ RESPONSE
        // =================================================

        const responseText =
            await response.text();


        let data;


        try {

            data =
                responseText
                    ? JSON.parse(
                        responseText
                    )
                    : {};

        }

        catch (error) {

            throw new Error(
                "Feedback API returned an invalid response."
            );

        }


        // =================================================
        // RESPONSE VALIDATION
        // =================================================

        if (!response.ok) {

            throw new Error(
                data.message ||
                data.error ||
                "Unable to save feedback."
            );

        }


        // =================================================
        // SUCCESS
        // =================================================

        if (feedbackMessage) {

            feedbackMessage.textContent =
                "✅ Thank you! Feedback recorded.";

            feedbackMessage.style.color =
                "#16a34a";

        }

    }


    catch (error) {

        console.error(
            "Feedback error:",
            error
        );


        if (feedbackMessage) {

            feedbackMessage.textContent =
                "❌ " +
                (
                    error.message ||
                    "Unable to save feedback."
                );

            feedbackMessage.style.color =
                "#dc2626";

        }

    }

}
