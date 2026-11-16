
package com.cropdisease.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/api/home")
    public String home() {
        return "Welcome to Crop Disease Reporting and Farm Advisory System!";
    }
}
