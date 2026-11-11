package com.epam.finaltask.dto;

import java.util.List;

import com.epam.finaltask.model.Voucher;

import jakarta.validation.constraints.*;
import lombok.Data;

public class UserDTO {

	private String id;

	@Size(min = 5, message = "minimum length is 5 characters")
	@NotBlank(message = "username is required")
	private String username;

	@NotBlank(message = "Password cannot be empty")
	@Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters")
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).+$",
			message = "Password must contain uppercase, lowercase, number and special character"
	)
	private String password;

	private String role;

	private List<Voucher> vouchers;

	@NotBlank(message = "phone number is required")
	@Pattern(
			regexp = "^\\+[1-9]\\d{7,14}$",
			message = "Invalid phone number format. Example: +48123456789"
	)
	private String phoneNumber;

	@NotNull
	@PositiveOrZero(message = "balance must be positive")
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
