package com.max.rental.models.enums;

import lombok.Getter;

@Getter
public enum EnumListingStatus {
    DRAFT(1),
    ACTIVE(2),
    INACTIVE(3);

    private final int value;

    EnumListingStatus(int value) {
        this.value = value;
    }

    public static EnumListingStatus fromValue(int value) {
        for (EnumListingStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown listing status value: " + value);
    }
}