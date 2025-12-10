package com.max.rental.dto.favorite;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteListingDto {

    private Long listingId;
    private String title;
    private String description;
    private BigDecimal pricePerNight;
    private String city;
    private String district;
    private String propertyType;
    private Double averageRating;
    private LocalDateTime addedAt;
}