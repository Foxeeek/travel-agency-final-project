package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotNull;

public class UserStatusRequestDTO {

    @NotNull(message = "{validation.required}")
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
