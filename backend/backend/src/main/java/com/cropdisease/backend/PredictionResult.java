package com.cropdisease.backend;

public class PredictionResult {

    private String disease;
    private double confidence;

    public PredictionResult() {
    }

    public PredictionResult(String disease, double confidence) {
        this.disease = disease;
        this.confidence = confidence;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}