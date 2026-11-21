package com.epam.finaltask.validation;


import com.epam.finaltask.dto.VoucherFilterDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FilterDateRangeValidator implements ConstraintValidator<ValidDateRange, VoucherFilterDTO> {


    @Override
    public boolean isValid(VoucherFilterDTO dto, ConstraintValidatorContext context) {
        if (dto.getArrivalFrom() == null || dto.getArrivalTo() == null) return true;
        if (!dto.getArrivalFrom().isAfter(dto.getArrivalTo())) return true;
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("arrivalTo").addConstraintViolation();
        return false;
    }
}
