package com.max.rental.dto.booking;

import com.max.rental.models.enums.EnumBookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class OwnerBookingDto {

    private Long id;

    private Long renterId;
    private String renterName;
    private String renterEmail;
    private String renterPhone;

    private LocalDate startDate;
    private LocalDate endDate;
    private long nights;

    private BigDecimal totalPrice;

    private EnumBookingStatus status;

    private LocalDateTime createdAt;
}