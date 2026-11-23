package com.epam.finaltask.dto;

import com.epam.finaltask.model.VoucherStatus;
import jakarta.validation.constraints.NotNull;

public class VoucherStatusRequestDTO {
    @NotNull(message = "{validation.required}")
    private VoucherStatus status;

    public VoucherStatus getStatus() {
        return status;
    }

    public void setStatus(VoucherStatus status) {
        this.status = status;
    }
}
