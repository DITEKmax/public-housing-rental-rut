package com.max.rental.dto.listing;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ListingFilterDto {
    private String city;
    private String propertyType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String district;
    private Integer floor;
}