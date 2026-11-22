package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherFilterDTO;
import com.epam.finaltask.model.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VoucherSpecifications {
    static Specification<Voucher> hasTourType(TourType type) {
        return type == null ? null : ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("tourType"), type));
    }

    static Specification<Voucher> hasHotelType(HotelType type) {
        return type == null ? null : ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("hotelType"), type));
    }

    static Specification<Voucher> hasTransferType(TransferType type) {
        return type == null ? null : ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("transferType"), type));
    }

    static Specification<Voucher> hasStatus(VoucherStatus type) {
        return type == null ? null : ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), type));
    }

    static Specification<Voucher> priceFrom(BigDecimal min) {
        return min == null ? null : ((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.<BigDecimal>get("price"), min));

    }

    static Specification<Voucher> priceTo(BigDecimal max) {
        return max == null ? null : ((root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.<BigDecimal>get("price"), max));
    }

    static Specification<Voucher> arrivesFrom(LocalDate from) {
        return from == null ? null : ((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.<LocalDate>get("arrivalDate"), from));
    }

    static Specification<Voucher> arrivesTo(LocalDate to) {
        return to == null ? null : ((root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.<LocalDate>get("arrivalDate"), to));
    }

    static Specification<Voucher> fromFilter(VoucherFilterDTO filter) {
        return Specification.where(hasTourType(filter.getTourType()))
                .and(hasHotelType(filter.getHotelType()))
                .and(hasTransferType(filter.getTransferType()))
                .and(hasStatus(filter.getStatus()))
                .and(priceFrom(filter.getMinPrice()))
                .and(priceTo(filter.getMaxPrice()))
                .and(arrivesFrom(filter.getArrivalFrom()))
                .and(arrivesTo(filter.getArrivalTo()));
    }
}
