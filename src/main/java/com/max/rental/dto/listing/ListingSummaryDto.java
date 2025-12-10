package com.max.rental.dto.listing;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ListingSummaryDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal pricePerNight;
    private String city;
    private String district;
    private String type;
    private Double rating = 0.0;
}