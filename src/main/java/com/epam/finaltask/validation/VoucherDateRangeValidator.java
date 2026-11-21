package com.epam.finaltask.validation;

import com.epam.finaltask.dto.VoucherDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VoucherDateRangeValidator implements ConstraintValidator<ValidDateRange, VoucherDTO> {

    @Override
    public boolean isValid(VoucherDTO dto, ConstraintValidatorContext context) {
        if (dto.getArrivalDate() == null || dto.getEvictionDate() == null) return true;
        if (!dto.getArrivalDate().isAfter(dto.getEvictionDate())) return true;
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("evictionDate").addConstraintViolation();
        return false;
    }
}
