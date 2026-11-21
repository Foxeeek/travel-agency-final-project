package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {


    @NotBlank(message = "must not be blank")
    private String username;

    @NotBlank(message = "must not be blank")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
