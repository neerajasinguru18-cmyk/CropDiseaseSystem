
package com.cropdisease.backend;

public class CropReport {

    private String farmerName;
    private String farmerEmail;
    private String cropName;
    private String cropType;
    private String symptoms;
    private String village;
    private String district;
    private String imageName;
    private String detectedDisease;

    public CropReport() {
    }

    // Existing constructor for compatibility
    public CropReport(String farmerName, String cropName,
                     String cropType, String symptoms,
                     String village, String district,
                     String imageName, String detectedDisease) {

        this.farmerName = farmerName;
        this.cropName = cropName;
        this.cropType = cropType;
        this.symptoms = symptoms;
        this.village = village;
        this.district = district;
        this.imageName = imageName;
        this.detectedDisease = detectedDisease;
    }

    // New constructor with farmer email
    public CropReport(String farmerName, String farmerEmail,
                     String cropName, String cropType,
                     String symptoms, String village,
                     String district, String imageName,
                     String detectedDisease) {

        this.farmerName = farmerName;
        this.farmerEmail = farmerEmail;
        this.cropName = cropName;
        this.cropType = cropType;
        this.symptoms = symptoms;
        this.village = village;
        this.district = district;
        this.imageName = imageName;
        this.detectedDisease = detectedDisease;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getFarmerEmail() {
        return farmerEmail;
    }

    public void setFarmerEmail(String farmerEmail) {
        this.farmerEmail = farmerEmail;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getDetectedDisease() {
        return detectedDisease;
    }

    public void setDetectedDisease(String detectedDisease) {
        this.detectedDisease = detectedDisease;
    }
}
