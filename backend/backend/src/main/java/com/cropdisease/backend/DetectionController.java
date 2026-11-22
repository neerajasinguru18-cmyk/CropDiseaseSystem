
package com.cropdisease.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/detection")
@CrossOrigin
public class DetectionController {

    private final DiseaseDetectionService detectionService;

    public DetectionController(DiseaseDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @PostMapping("/predict")
    public ResponseEntity<?> predictDisease(
            @RequestParam("image") MultipartFile image) {

        try {
            PredictionResult result = detectionService.predict(image);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Prediction failed: " + e.getMessage());
        }
    }
}
