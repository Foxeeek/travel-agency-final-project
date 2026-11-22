package com.epam.finaltask.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {VoucherDateRangeValidator.class, FilterDateRangeValidator.class})
public @interface ValidDateRange {
    String message() default "eviction date must not be before arrival date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}