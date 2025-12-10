package com.max.rental.models.enums;

import lombok.Getter;

@Getter
public enum EnumListingSortField {
    PRICE(1), RATING(2), DATE(3);

    private final int VALUE;

    EnumListingSortField(int VALUE){
        this.VALUE = VALUE;
    }

}
