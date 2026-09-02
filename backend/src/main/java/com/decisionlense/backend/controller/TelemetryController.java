
package com.decisionlense.backend.controller;

import com.decisionlense.backend.service.TelemetryService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/telemetry")
@CrossOrigin(
        origins = {
                "http://localhost:5500",
                "http://127.0.0.1:5500"
        },
        allowCredentials = "true"
)
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(
            TelemetryService telemetryService
    ) {
        this.telemetryService =
                telemetryService;
    }

    @GetMapping
    public ResponseEntity<?> getTelemetry(
            Authentication authentication
    ) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(401)
                    .body(
                            java.util.Map.of(
                                    "success",
                                    false,
                                    "message",
                                    "Authentication required."
                            )
                    );
        }

        return ResponseEntity.ok(
                telemetryService.snapshot()
        );
    }
}