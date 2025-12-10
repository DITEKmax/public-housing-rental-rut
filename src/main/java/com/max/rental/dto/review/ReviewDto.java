package com.max.rental.dto.review;


import lombok.Data;

import java.time.LocalDate;

@Data
public class ReviewDto {
    private String guestName;
    private Integer rating;
    private String comment;
    private LocalDate reviewDate;
}
