package com.max.rental.dto.listing;

import com.max.rental.models.enums.EnumListingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OwnerListingDto {

    private Long id;
    private String title;
    private String description;
    private BigDecimal pricePerNight;

    private String city;
    private String district;

    private String propertyType;

    private Integer roomCount;
    private Integer floor;
    private Integer totalFloors;
    private Integer constructionYear;
    private String rules;

    private EnumListingStatus status;
    private Double averageRating;

    private int totalBookings;
    private int activeBookings;
    private int reviewCount;

    private LocalDateTime createdAt;
}