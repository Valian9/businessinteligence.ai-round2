package com.decisionlense.backend.controller;

import com.decisionlense.backend.service.MultiSourceAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class MultiSourceAnalysisController {

    private final MultiSourceAnalysisService analysisService;

    public MultiSourceAnalysisController(
            MultiSourceAnalysisService analysisService
    ) {
        this.analysisService = analysisService;
    }

    @PostMapping(
            value = "/multi-source",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> analyzeMultiSource(

            @RequestParam("sales")
            MultipartFile salesFile,

            @RequestParam("inventory")
            MultipartFile inventoryFile,

            @RequestParam("marketing")
            MultipartFile marketingFile

    ) {

        try {

            if (salesFile == null || salesFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Sales file is missing or empty.");
            }

            if (inventoryFile == null || inventoryFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Inventory file is missing or empty.");
            }

            if (marketingFile == null || marketingFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Marketing file is missing or empty.");
            }

            // Directly call the service method without declaring a concrete
            // response type here; the service method already defines the return type.
            var response = analysisService.analyze(
                    salesFile,
                    inventoryFile,
                    marketingFile
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            "Multi-source analysis failed: "
                                    + e.getMessage()
                    );
        }
    }
}