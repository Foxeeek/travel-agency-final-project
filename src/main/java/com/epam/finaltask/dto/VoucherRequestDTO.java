package com.epam.finaltask.dto;

import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.validation.ValidDateRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@ValidDateRange
public class VoucherRequestDTO {
    @NotBlank(message = "must not be blank")
    private String title;
    @NotBlank(message = "must not be blank")
    private String description;
    @NotNull(message = "is required")
    @Positive(message = "must be greater than 0")
    private Double price;
    @NotNull(message = "is required")
    private TourType tourType;
    @NotNull(message = "is required")
    private TransferType transferType;
    @NotNull(message = "is required")
    private HotelType hotelType;
    @NotNull(message = "is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate arrivalDate;
    @NotNull(message = "is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate evictionDate;
    @NotNull(message = "is required")
    private Boolean isHot;

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEvictionDate() {
        return evictionDate;
    }

    public void setEvictionDate(LocalDate evictionDate) {
        this.evictionDate = evictionDate;
    }

    public HotelType getHotelType() {
        return hotelType;
    }

    public void setHotelType(HotelType hotelType) {
        this.hotelType = hotelType;
    }

    public Boolean getIsHot() {
        return isHot;
    }

    public void setIsHot(Boolean isHot) {
        this.isHot = isHot;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TourType getTourType() {
        return tourType;
    }

    public void setTourType(TourType tourType) {
        this.tourType = tourType;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }
}
