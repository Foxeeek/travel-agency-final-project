package com.epam.finaltask.dto;

import java.util.List;

import com.epam.finaltask.model.Voucher;

import jakarta.validation.constraints.*;
import lombok.Data;

public class UserDTO {

	private String id;

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

	private String role;

	private List<Voucher> vouchers;

	@NotBlank(message = "is required")
	@Pattern(
			regexp = "^\\+[1-9]\\d{7,14}$",
			message = "must be in international format, e.g. +380123456789"
	)
	private String phoneNumber;

	@NotNull(message = "is required")
	@PositiveOrZero(message = "must not be negative")
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
