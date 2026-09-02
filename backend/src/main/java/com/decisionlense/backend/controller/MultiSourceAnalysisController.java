package com.decisionlense.backend.controller;

import com.decisionlense.backend.service.MultiSourceAnalysisService;
import com.decisionlense.backend.service.TelemetryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class MultiSourceAnalysisController {

    private final MultiSourceAnalysisService multiSourceAnalysisService;
    private final TelemetryService telemetryService;

    public MultiSourceAnalysisController(
            MultiSourceAnalysisService multiSourceAnalysisService,
            TelemetryService telemetryService
    ) {
        this.multiSourceAnalysisService = multiSourceAnalysisService;
        this.telemetryService = telemetryService;
    }

    @PostMapping("/multi-source")
    public ResponseEntity<MultiSourceAnalysisService.MultiSourceAnalysisResponse> analyzeMultiSource(

            @RequestParam("salesFile")
            MultipartFile salesFile,

            @RequestParam("inventoryFile")
            MultipartFile inventoryFile,

            @RequestParam("marketingFile")
            MultipartFile marketingFile,

            @RequestParam(defaultValue = "Supply Chain Manager")
            String persona

    ) throws IOException {

        // =====================================================
        // TELEMETRY START
        // =====================================================

        long telemetryStart =
                telemetryService.startTimer();

        try {

            // =====================================================
            // EXISTING ANALYSIS LOGIC
            // =====================================================

            MultiSourceAnalysisService.MultiSourceAnalysisResponse response =
                    multiSourceAnalysisService.analyze(
                            salesFile,
                            inventoryFile,
                            marketingFile,
                            persona
                    );

            // =====================================================
            // TELEMETRY SUCCESS
            // =====================================================

            boolean abstained =
                    response.isAbstained();

            telemetryService.recordRequest(
                    telemetryStart,
                    true,
                    abstained,
                    0,
                    0,
                    0.0
            );

            // =====================================================
            // RETURN RESPONSE
            // =====================================================

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            // =====================================================
            // TELEMETRY FAILURE
            // =====================================================

            telemetryService.recordRequest(
                    telemetryStart,
                    false,
                    false,
                    0,
                    0,
                    0.0
            );

            throw e;
        }
    }
}