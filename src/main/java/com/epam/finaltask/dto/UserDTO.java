package com.epam.finaltask.dto;

import com.epam.finaltask.model.Voucher;
import jakarta.validation.constraints.*;

import java.util.List;

public class UserDTO {

    private String id;

    @Size(min = 5, message = "{validation.username.size}")
    @NotBlank(message = "{validation.notBlank}")
    private String username;

    @NotBlank(message = "{validation.notBlank}")
    @Size(min = 8, max = 32, message = "{validation.password.size}")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).+$",
            message = "{validation.password.pattern}"
    )
    private String password;

    private String role;

    private List<Voucher> vouchers;

    @NotBlank(message = "{validation.required}")
    @Pattern(
            regexp = "^\\+[1-9]\\d{7,14}$",
            message = "{validation.phone.pattern}"
    )
    private String phoneNumber;

    @NotNull(message = "{validation.required}")
    @PositiveOrZero(message = "{validation.notNegative}")
    private Double balance;


    private boolean active;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Voucher> getVouchers() {
        return vouchers;
    }

    public void setVouchers(List<Voucher> vouchers) {
        this.vouchers = vouchers;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
