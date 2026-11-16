
package com.cropdisease.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin
public class ReportController {

    private final ObjectMapper mapper = new ObjectMapper();

    private final File reportFile =
            new File("../../data/reports.json");

    private final Path uploadFolder =
            Paths.get("../../uploads").toAbsolutePath().normalize();

    // API 1: Submit Crop Disease Report

    @PostMapping("/submit")
    public ResponseEntity<String> submitReport(

            @RequestParam("farmerName") String farmerName,
            @RequestParam("cropName") String cropName,
            @RequestParam("cropType") String cropType,
            @RequestParam("symptoms") String symptoms,
            @RequestParam("village") String village,
            @RequestParam("district") String district,
            @RequestParam("image") MultipartFile image) {

        try {

            if (farmerName.isBlank() ||
                cropName.isBlank() ||
                cropType.isBlank() ||
                symptoms.isBlank() ||
                village.isBlank() ||
                district.isBlank()) {

                return ResponseEntity.badRequest()
                        .body("Please fill all fields!");
            }

            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Please upload a crop image!");
            }

            if (image.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body("Image size must be below 5 MB!");
            }

            if (image.getContentType() == null ||
                !image.getContentType().startsWith("image/")) {

                return ResponseEntity.badRequest()
                        .body("Please upload a valid image!");
            }

            String originalName = image.getOriginalFilename();

            String extension = "";

            if (originalName != null &&
                originalName.lastIndexOf(".") >= 0) {

                extension = originalName.substring(
                        originalName.lastIndexOf("."))
                        .toLowerCase(Locale.ROOT);
            }

            if (!List.of(".jpg", ".jpeg", ".png", ".webp")
                    .contains(extension)) {

                return ResponseEntity.badRequest()
                        .body("Only JPG, JPEG, PNG and WEBP images are allowed!");
            }

            Files.createDirectories(uploadFolder);

            String imageName = UUID.randomUUID() + extension;

            Path imagePath = uploadFolder.resolve(imageName);

            image.transferTo(imagePath.toFile());

            List<CropReport> reports = new ArrayList<>();

            if (reportFile.exists() &&
                reportFile.length() > 0) {

                reports = mapper.readValue(
                    reportFile,
                    new TypeReference<List<CropReport>>() {}
                );
            }

            CropReport report = new CropReport(
                    farmerName,
                    cropName,
                    cropType,
                    symptoms,
                    village,
                    district,
                    imageName
            );

            reports.add(report);

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(reportFile, reports);

            return ResponseEntity.ok(
                    "Crop disease report submitted successfully!"
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body("Unable to submit report!");
        }
    }


    // API 2: Get All Submitted Reports

    @GetMapping("/all")
    public ResponseEntity<?> getAllReports() {

        try {

            if (!reportFile.exists() ||
                reportFile.length() == 0) {

                return ResponseEntity.ok(
                        new ArrayList<CropReport>()
                );
            }

            List<CropReport> reports = mapper.readValue(
                    reportFile,
                    new TypeReference<List<CropReport>>() {}
            );

            return ResponseEntity.ok(reports);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body("Unable to load reports!");
        }
    }

}
