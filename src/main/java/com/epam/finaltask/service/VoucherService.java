package com.epam.finaltask.service;

import com.epam.finaltask.dto.PageResponseDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherFilterDTO;
import com.epam.finaltask.dto.VoucherRequestDTO;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.VoucherStatus;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VoucherService {
    VoucherDTO create(VoucherRequestDTO request);

    VoucherDTO update(String id, VoucherRequestDTO request);

    VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO);

    VoucherDTO findById(String id);

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

    PageResponseDTO<VoucherDTO> search(VoucherFilterDTO filter, Pageable pageable);

}