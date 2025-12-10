package com.max.rental.dto.listing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PopularCityDto {
    private String city;
    private Long listingCount;
    private Long bookingCount;

    public Long getPopularityScore() {

        return (bookingCount != null ? bookingCount * 2 : 0L) + (listingCount != null ? listingCount : 0L);
    }
}
