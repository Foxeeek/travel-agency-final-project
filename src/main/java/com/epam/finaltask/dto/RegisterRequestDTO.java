package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequestDTO {

    @Size(min = 5, message = "must be at least 5 characters long")
    @NotBlank(message = "must not be blank")
    private String username;

    @NotBlank(message = "must not be blank")
    @Size(min = 8, max = 32, message = "must be 8 to 32 characters long")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).+$",
            message = "must contain an uppercase letter, a lowercase letter, a digit and a special character (@#$%^&+=!)"
    )
    private String password;

    @NotBlank(message = "is required")
    @Pattern(
            regexp = "^\\+[1-9]\\d{7,14}$",
            message = "must be in international format, e.g. +123 456789"
    )
    private String phoneNumber;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
