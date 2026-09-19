package com.vaanistock.auth;

import com.vaanistock.business.BusinessType;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private UserDto user;
    private BusinessDto business;

    public AuthResponse() {}

    public AuthResponse(String token, UserDto user, BusinessDto business) {
        this.token = token;
        this.user = user;
        this.business = business;
    }

    public static class UserDto {
        private Long id;
        private String name;
        private String mobile;
        private String email;
        private String preferredLanguage;

        public UserDto() {}

        public UserDto(Long id, String name, String mobile, String email, String preferredLanguage) {
            this.id = id;
            this.name = name;
            this.mobile = mobile;
            this.email = email;
            this.preferredLanguage = preferredLanguage;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

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

        public String getPreferredLanguage() {
            return preferredLanguage;
        }

        public void setPreferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
        }
    }

    public static class BusinessDto {
        private Long id;
        private String businessName;
        private BusinessType businessType;
        private String location;

        public BusinessDto() {}

        public BusinessDto(Long id, String businessName, BusinessType businessType, String location) {
            this.id = id;
            this.businessName = businessName;
            this.businessType = businessType;
            this.location = location;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public BusinessDto getBusiness() {
        return business;
    }

    public void setBusiness(BusinessDto business) {
        this.business = business;
    }
}
