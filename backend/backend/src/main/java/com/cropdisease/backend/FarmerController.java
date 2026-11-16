
package com.cropdisease.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/api/farmers")
@CrossOrigin
public class FarmerController {

    private final ObjectMapper mapper = new ObjectMapper();

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    private final File file =
            new File("../../data/farmers.json");

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody Farmer farmer) {

        try {

            // 1. Check empty fields
            if (farmer == null ||
                isEmpty(farmer.getName()) ||
                isEmpty(farmer.getMobile()) ||
                isEmpty(farmer.getEmail()) ||
                isEmpty(farmer.getVillage()) ||
                isEmpty(farmer.getDistrict()) ||
                isEmpty(farmer.getPassword())) {

                return ResponseEntity.badRequest()
                        .body("All fields are required!");
            }

            // 2. Validate mobile number
            if (!farmer.getMobile().matches("[0-9]{10}")) {

                return ResponseEntity.badRequest()
                        .body("Enter a valid 10-digit mobile number!");
            }

            // 3. Validate email
            if (!farmer.getEmail().matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

                return ResponseEntity.badRequest()
                        .body("Enter a valid email address!");
            }

            // 4. Validate password
            if (farmer.getPassword().length() < 8) {

                return ResponseEntity.badRequest()
                        .body("Password must contain at least 8 characters!");
            }

            // 5. Read existing farmers
            List<Farmer> farmers = new ArrayList<>();

            if (file.exists()) {

                farmers = mapper.readValue(
                        file,
                        new TypeReference<List<Farmer>>() {}
                );
            }

            // 6. Check duplicate email and mobile
            for (Farmer existing : farmers) {

                if (existing.getEmail() != null &&
                    existing.getEmail().equalsIgnoreCase(
                            farmer.getEmail().trim())) {

                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Email is already registered!");
                }

                if (existing.getMobile() != null &&
                    existing.getMobile().equals(
                            farmer.getMobile().trim())) {

                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Mobile number is already registered!");
                }
            }

            // 7. Clean up text fields
            farmer.setName(farmer.getName().trim());
            farmer.setMobile(farmer.getMobile().trim());
            farmer.setEmail(farmer.getEmail().trim());
            farmer.setVillage(farmer.getVillage().trim());
            farmer.setDistrict(farmer.getDistrict().trim());

            // 8. Encrypt password using BCrypt
            farmer.setPassword(
                    passwordEncoder.encode(farmer.getPassword())
            );

            // 9. Add farmer to list
            farmers.add(farmer);

            // 10. Create data folder if required
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            // 11. Save farmers in JSON file
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, farmers);

            return ResponseEntity.ok(
                    "Farmer registered successfully!"
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body("Registration failed! Please try again.");
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
