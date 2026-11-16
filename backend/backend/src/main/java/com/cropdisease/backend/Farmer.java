
package com.cropdisease.backend;

public class Farmer {

    private String name;
    private String mobile;
    private String email;
    private String village;
    private String district;
    private String password;

    public Farmer() {
    }

    public Farmer(String name, String mobile, String email,
                  String village, String district, String password) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.village = village;
        this.district = district;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public String getVillage() {
        return village;
    }

    public String getDistrict() {
        return district;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
