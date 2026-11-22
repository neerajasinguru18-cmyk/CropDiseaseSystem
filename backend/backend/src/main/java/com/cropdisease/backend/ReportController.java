
package com.cropdisease.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

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
            @RequestParam("image") MultipartFile image,

            @RequestParam(value = "detectedDisease",
                    required = false,
                    defaultValue = "Detection pending")
            String detectedDisease,

            @RequestParam(value = "confidence",
                    required = false,
                    defaultValue = "0")
            double confidence,

            HttpSession session) {

        try {

            // Verify farmer login session

            String loggedInEmail =
                    (String) session.getAttribute("farmerEmail");

            String loggedInName =
                    (String) session.getAttribute("farmerName");

            if (loggedInEmail == null || loggedInName == null) {

                return ResponseEntity.status(401)
                        .body("Please login before submitting a report!");
            }

            // Validate farmer information

            if (cropName.isBlank() ||
                cropType.isBlank() ||
                symptoms.isBlank() ||
                village.isBlank() ||
                district.isBlank()) {

                return ResponseEntity.badRequest()
                        .body("Please fill all fields!");
            }

            // Validate image

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

            // Save uploaded image

            Files.createDirectories(uploadFolder);

            String imageName = UUID.randomUUID() + extension;

            Path imagePath = uploadFolder.resolve(imageName);

            image.transferTo(imagePath.toFile());

            // Read existing reports

            List<CropReport> reports = new ArrayList<>();

            if (reportFile.exists() &&
                reportFile.length() > 0) {

                reports = mapper.readValue(
                    reportFile,
                    new TypeReference<List<CropReport>>() {}
                );
            }

            // Create report using logged-in farmer identity

            CropReport report = new CropReport(
                    loggedInName,
                    loggedInEmail.toLowerCase(Locale.ROOT),
                    cropName,
                    cropType,
                    symptoms,
                    village,
                    district,
                    imageName,
                    detectedDisease
            );

            reports.add(report);

            // Create data directory if required

            File parent = reportFile.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            // Save report

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


    // API 2: Get Reports of Logged-In Farmer Only

    @GetMapping("/all")
    public ResponseEntity<?> getAllReports(HttpSession session) {

        try {

            // Verify login session

            String loggedInEmail =
                    (String) session.getAttribute("farmerEmail");

            if (loggedInEmail == null) {

                return ResponseEntity.status(401)
                        .body("Please login to view your reports!");
            }

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

            // Filter reports using logged-in farmer email

            List<CropReport> farmerReports = new ArrayList<>();

            for (CropReport report : reports) {

                if (report.getFarmerEmail() != null &&
                    report.getFarmerEmail().equalsIgnoreCase(
                            loggedInEmail.trim())) {

                    farmerReports.add(report);
                }
            }

            return ResponseEntity.ok(farmerReports);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body("Unable to load reports!");
        }
    }

}