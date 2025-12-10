package com.max.rental.models.enums;

import lombok.Getter;

@Getter
public enum EnumPropertyType {
    APARTMENT("APARTMENT", "Квартира"),
    HOUSE("HOUSE", "Дом"),
    ROOM("ROOM", "Комната");

    private final String code;
    private final String displayName;

    EnumPropertyType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
}
