package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

public class VoucherDTO {

    private String id;

    @NotBlank(message = "{validation.notBlank}")
    private String title;
    @NotBlank(message = "{validation.notBlank}")
    private String description;

    @Positive(message = "{validation.positive}")
    @NotNull(message = "{validation.required}")
    private Double price;

    @NotBlank(message = "{validation.required}")
    private String tourType;

    @NotBlank(message = "{validation.required}")
    private String transferType;

    @NotBlank(message = "{validation.required}")
    private String hotelType;

    @NotBlank(message = "{validation.required}")
    private String status;

    @NotNull(message = "{validation.required}")
    private LocalDate arrivalDate;

    @NotNull(message = "{validation.required}")
    private LocalDate evictionDate;

    private UUID userId;

    @NotNull(message = "{validation.required}")
    private Boolean isHot;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }


    public String getTourType() {
        return tourType;
    }

    public void setTourType(String tourType) {
        this.tourType = tourType;
    }


    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }


    public String getHotelType() {
        return hotelType;
    }

    public void setHotelType(String hotelType) {
        this.hotelType = hotelType;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }


    public LocalDate getEvictionDate() {
        return evictionDate;
    }

    public void setEvictionDate(LocalDate evictionDate) {
        this.evictionDate = evictionDate;
    }


    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }


    public Boolean getIsHot() {
        return isHot;
    }

    public void setIsHot(Boolean isHot) {
        this.isHot = isHot;
    }

}
