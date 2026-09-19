package com.vaanistock.auth;

import com.vaanistock.business.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    @NotBlank(message = "Owner name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 15, message = "Mobile number should be between 10 and 15 digits")
    private String mobile;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String preferredLanguage = "te"; // 'en', 'te', 'hi'

    @NotBlank(message = "Business name is required")
    private String businessName;

    private BusinessType businessType = BusinessType.WHOLESALE;

    @NotBlank(message = "Location is required")
    private String location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
