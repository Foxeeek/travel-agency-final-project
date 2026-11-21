package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.VoucherStatus;

import java.util.UUID;

public interface VoucherOrderService {
    VoucherDTO order(UUID id, UUID userId);

    VoucherDTO changeStatus(UUID id, VoucherStatus status);
}
