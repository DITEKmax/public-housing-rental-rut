package com.max.rental.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationDto {
    private Long nights;
    private BigDecimal pricePerNight;
    private BigDecimal subtotal;
    private BigDecimal serviceFee;
    private BigDecimal total;
}
