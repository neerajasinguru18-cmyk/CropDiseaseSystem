
package com.cropdisease.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/farmers")
@CrossOrigin
public class LoginController {

    private final ObjectMapper mapper = new ObjectMapper();

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    private final File file =
            new File("../../data/farmers.json");

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody Farmer farmer,
            HttpSession session) {

        try {

            if (farmer.getEmail() == null ||
                farmer.getPassword() == null) {

                return ResponseEntity.badRequest()
                        .body("Email and password are required!");
            }

            List<Farmer> farmers = new ArrayList<>();

            if (file.exists()) {

                farmers = mapper.readValue(
                        file,
                        new TypeReference<List<Farmer>>() {}
                );
            }

            for (Farmer existing : farmers) {

                if (existing.getEmail() != null &&
                    existing.getEmail().equalsIgnoreCase(
                        farmer.getEmail().trim())) {

                    if (passwordEncoder.matches(
                            farmer.getPassword(),
                            existing.getPassword())) {

                        // Store logged-in farmer identity
                        session.setAttribute(
                                "farmerEmail",
                                existing.getEmail().trim().toLowerCase()
                        );

                        session.setAttribute(
                                "farmerName",
                                existing.getName()
                        );

                        return ResponseEntity.ok(
                                "Login successful! Welcome, "
                                + existing.getName()
                        );
                    }

                    return ResponseEntity.status(401)
                            .body("Incorrect password!");
                }
            }

            return ResponseEntity.status(404)
                    .body("Farmer account not found!");

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body("Login failed! Please try again.");
        }
    }

    // Check currently logged-in farmer
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentFarmer(
            HttpSession session) {

        String email = (String) session.getAttribute("farmerEmail");

        if (email == null) {
            return ResponseEntity.status(401)
                    .body("Please login first!");
        }

        return ResponseEntity.ok(email);
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok("Logged out successfully!");
    }
}