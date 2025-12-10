package com.max.rental.dto.booking;

import com.max.rental.models.enums.EnumBookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingHistoryDto {
    private Long id;
    private Long listingId;
    private String listingTitle;
    private String city;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private EnumBookingStatus status;
    private boolean hasReview;
    private boolean canLeaveReview;
}