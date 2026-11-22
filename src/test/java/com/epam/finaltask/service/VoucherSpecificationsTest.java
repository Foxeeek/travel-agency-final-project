package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherFilterDTO;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.VoucherRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class VoucherSpecificationsTest {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private EntityManager entityManager;

    private Voucher voucher(String title, TourType tour, HotelType hotel, TransferType transfer,
                            VoucherStatus status, String price, LocalDate arrival) {
        Voucher voucher = new Voucher();
        voucher.setTitle(title);
        voucher.setDescription("Description of " + title);
        voucher.setPrice(new BigDecimal(price));
        voucher.setTourType(tour);
        voucher.setHotelType(hotel);
        voucher.setTransferType(transfer);
        voucher.setStatus(status);
        voucher.setArrivalDate(arrival);
        voucher.setEvictionDate(arrival.plusDays(5));
        voucher.setIsHot(false);
        return voucher;
    }

    @BeforeEach
    void seed() {
        voucherRepository.saveAll(List.of(
                voucher("A", TourType.ADVENTURE, HotelType.FIVE_STARS, TransferType.PLANE,
                        VoucherStatus.REGISTERED, "850.00", LocalDate.of(2027, 6, 10)),
                voucher("B", TourType.CULTURAL, HotelType.THREE_STARS, TransferType.TRAIN,
                        VoucherStatus.REGISTERED, "420.00", LocalDate.of(2027, 4, 12)),
                voucher("C", TourType.CULTURAL, HotelType.FOUR_STARS, TransferType.TRAIN,
                        VoucherStatus.PAID, "510.00", LocalDate.of(2027, 3, 20)),
                voucher("D", TourType.WINE, HotelType.FOUR_STARS, TransferType.PLANE,
                        VoucherStatus.REGISTERED, "1200.00", LocalDate.of(2027, 9, 5))
        ));
        entityManager.flush();
        entityManager.clear();
    }

    private Set<String> titlesFor(VoucherFilterDTO filter) {
        return voucherRepository.findAll(VoucherSpecifications.fromFilter(filter)).stream()
                .map(Voucher::getTitle)
                .collect(Collectors.toSet());
    }

    @Test
    void fromFilter_EmptyFilter_ReturnsAllVouchers() {
        assertEquals(Set.of("A", "B", "C", "D"), titlesFor(new VoucherFilterDTO()));
    }

    @Test
    void fromFilter_TourType_ReturnsOnlyMatching() {
        VoucherFilterDTO filter = new VoucherFilterDTO();
        filter.setTourType(TourType.CULTURAL);

        assertEquals(Set.of("B", "C"), titlesFor(filter));
    }

    @Test
    void fromFilter_StatusAndTransfer_ReturnsOnlyMatching() {
        VoucherFilterDTO filter = new VoucherFilterDTO();
        filter.setStatus(VoucherStatus.PAID);
        filter.setTransferType(TransferType.TRAIN);

        assertEquals(Set.of("C"), titlesFor(filter));
    }

    @Test
    void fromFilter_PriceRange_IncludesBothBounds() {
        VoucherFilterDTO filter = new VoucherFilterDTO();
        filter.setMinPrice(new BigDecimal("420.00"));
        filter.setMaxPrice(new BigDecimal("850.00"));

        assertEquals(Set.of("A", "B", "C"), titlesFor(filter));
    }

    @Test
    void fromFilter_ArrivalPeriod_IncludesBothBounds() {
        VoucherFilterDTO filter = new VoucherFilterDTO();
        filter.setArrivalFrom(LocalDate.of(2027, 3, 20));
        filter.setArrivalTo(LocalDate.of(2027, 6, 10));

        assertEquals(Set.of("A", "B", "C"), titlesFor(filter));
    }

    @Test
    void fromFilter_SeveralFilters_AreCombinedWithAnd() {
        VoucherFilterDTO filter = new VoucherFilterDTO();
        filter.setHotelType(HotelType.FOUR_STARS);
        filter.setMaxPrice(new BigDecimal("600.00"));

        assertEquals(Set.of("C"), titlesFor(filter));
    }

    @Test
    void fromFilter_HotelTypeWithNoMatches_ReturnsEmpty() {
        VoucherFilterDTO filter = new VoucherFilterDTO();
        filter.setHotelType(HotelType.ONE_STAR);

        assertEquals(Set.of(), titlesFor(filter));
    }
}
