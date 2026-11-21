package com.epam.finaltask.repository;

import com.epam.finaltask.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VoucherRepository extends JpaRepository<Voucher, UUID>, JpaSpecificationExecutor<Voucher> {
    List<Voucher> findAllByUserId(UUID userId);

    List<Voucher> findAllByTourType(TourType tourType);

    List<Voucher> findAllByTransferType(TransferType transferType);

    List<Voucher> findAllByPrice(Double price);

    List<Voucher> findAllByHotelType(HotelType hotelType);

    @Query("SELECT v FROM Voucher v ORDER BY v.isHot DESC, v.price ")
    List<Voucher> findAllHotFirst();

    @Query("SELECT v FROM Voucher v WHERE v.price BETWEEN :min AND :max ORDER BY v.isHot DESC, v.price")
    List<Voucher> findAllByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query("SELECT v FROM Voucher v WHERE v.user.id = :userId AND v.status = :status")
    List<Voucher> findAllByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") VoucherStatus status);


    @Query("SELECT v FROM Voucher v WHERE v.arrivalDate BETWEEN :from AND :to ORDER BY v.arrivalDate")
    List<Voucher> findAllByArrivalPeriod(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
