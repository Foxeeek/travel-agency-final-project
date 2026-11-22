package com.epam.finaltask.validation;

import com.epam.finaltask.dto.VoucherRequestDTO;
import com.epam.finaltask.dto.VoucherFilterDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidDateRangeTest {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /** Property paths of violations produced only by @ValidDateRange (other constraints are ignored). */
    private List<String> dateRangeViolations(Object dto) {
        return validator.validate(dto).stream()
                .filter(violation -> violation.getConstraintDescriptor().getAnnotation() instanceof ValidDateRange)
                .map(violation -> violation.getPropertyPath().toString())
                .toList();
    }

    private VoucherRequestDTO voucher(LocalDate arrival, LocalDate eviction) {
        VoucherRequestDTO dto = new VoucherRequestDTO();
        dto.setArrivalDate(arrival);
        dto.setEvictionDate(eviction);
        return dto;
    }

    private VoucherFilterDTO filter(LocalDate from, LocalDate to) {
        VoucherFilterDTO dto = new VoucherFilterDTO();
        dto.setArrivalFrom(from);
        dto.setArrivalTo(to);
        return dto;
    }

    @Test
    void voucher_ArrivalAfterEviction_ViolationOnEvictionDate() {
        VoucherRequestDTO dto = voucher(LocalDate.of(2027, 9, 10), LocalDate.of(2027, 9, 1));

        assertEquals(List.of("evictionDate"), dateRangeViolations(dto));
    }

    @Test
    void voucher_ArrivalBeforeEviction_NoViolation() {
        VoucherRequestDTO dto = voucher(LocalDate.of(2027, 9, 1), LocalDate.of(2027, 9, 10));

        assertEquals(List.of(), dateRangeViolations(dto));
    }

    @Test
    void voucher_SameDay_NoViolation() {
        VoucherRequestDTO dto = voucher(LocalDate.of(2027, 9, 1), LocalDate.of(2027, 9, 1));

        assertEquals(List.of(), dateRangeViolations(dto));
    }

    @Test
    void voucher_MissingDate_NoViolationFromThisConstraint() {
        VoucherRequestDTO dto = voucher(LocalDate.of(2027, 9, 1), null);

        assertEquals(List.of(), dateRangeViolations(dto));
    }

    @Test
    void filter_FromAfterTo_ViolationOnArrivalTo() {
        VoucherFilterDTO dto = filter(LocalDate.of(2027, 8, 31), LocalDate.of(2027, 6, 1));

        assertEquals(List.of("arrivalTo"), dateRangeViolations(dto));
    }

    @Test
    void filter_EqualDates_NoViolation() {
        VoucherFilterDTO dto = filter(LocalDate.of(2027, 6, 10), LocalDate.of(2027, 6, 10));

        assertEquals(List.of(), dateRangeViolations(dto));
    }

    @Test
    void filter_OnlyOneDateSet_NoViolation() {
        assertEquals(List.of(), dateRangeViolations(filter(LocalDate.of(2027, 6, 10), null)));
        assertEquals(List.of(), dateRangeViolations(filter(null, LocalDate.of(2027, 6, 10))));
    }

    @Test
    void filter_NoDates_NoViolation() {
        assertEquals(List.of(), dateRangeViolations(new VoucherFilterDTO()));
    }
}
