package com.max.rental.dto.listing;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ListingEditDto {

    private Long id;

    private String title;
    private String description;
    private BigDecimal pricePerNight;

    // Тип жилья (APARTMENT, HOUSE, ROOM)
    private String propertyType;

    private String city;
    private String district;

    private Integer roomCount;
    private Integer floor;
    private Integer totalFloors;
    private Integer constructionYear;

    private String rules;

    private String status;
}