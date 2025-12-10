package com.max.rental.models.enums;

import lombok.Getter;


@Getter
public enum EnumBookingStatus {
    PENDING(1),
    CONFIRMED(2),
    CANCELLED(3),
    COMPLETED(4);

    private final int value;

    EnumBookingStatus(int value) {
        this.value = value;
    }


    public static EnumBookingStatus fromValue(int value) {
        for (EnumBookingStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown booking status value: " + value);
    }
}