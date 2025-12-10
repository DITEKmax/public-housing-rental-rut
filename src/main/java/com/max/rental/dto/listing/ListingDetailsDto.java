package com.max.rental.dto.listing;

import com.max.rental.dto.review.ReviewDto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ListingDetailsDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal pricePerNight;
    private String city;
    private String district;
    private String propertyType;
    private Integer roomCount;
    private Integer constructionYear;
    private String rules;
    private Integer floor;
    private Integer totalFloors;

    private Long ownerId;
    private String ownerFullName;
    private Double ownerRating;

    private Double averageRating;
    private List<ReviewDto> reviews;

    private boolean isAvailableForBooking;

    private boolean isCurrentUserOwner;

    private boolean isFavorite;

    public Integer getReviewCount() {
        return reviews != null ? reviews.size() : 0;
    }

    public Integer getRooms() {
        return roomCount;
    }

    public Integer getYearBuilt() {
        return constructionYear;
    }

    private Integer area;

    private Boolean hasWifi;
    private Boolean hasWashingMachine;
    private Boolean hasParking;
    private Boolean hasKitchen;
    private Boolean hasAirConditioning;
    private Boolean hasBalcony;
}