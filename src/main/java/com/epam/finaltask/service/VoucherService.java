package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.Voucher;
import com.epam.finaltask.model.VoucherStatus;
import org.springframework.data.jpa.repository.Query;

public interface VoucherService {
    VoucherDTO create(VoucherDTO voucherDTO);
    VoucherDTO order(String id, String userId);
    VoucherDTO update(String id, VoucherDTO voucherDTO);
    VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO);
    VoucherDTO changeStatus(UUID id, VoucherStatus status);

    void delete(String voucherId);

    List<VoucherDTO> findAllByUserId(String userId);

    List<VoucherDTO> findAllByTourType(TourType tourType);
    List<VoucherDTO> findAllByTransferType(String transferType);
    List<VoucherDTO> findAllByPrice(Double price);
    List<VoucherDTO> findAllByHotelType(HotelType hotelType);
    List<VoucherDTO> findAll();
    List<VoucherDTO> findAllHotFirst();
    List<VoucherDTO> findAllByPriceRange(BigDecimal min, BigDecimal max);
    List<VoucherDTO> findAllByUserIdAndStatus(UUID userId, VoucherStatus status);
    List<VoucherDTO> findAllByArrivalPeriod(LocalDate from, LocalDate to);

}