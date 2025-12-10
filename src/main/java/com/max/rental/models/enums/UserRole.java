package com.max.rental.models.enums;

import lombok.Getter;


@Getter
public enum UserRole {
    GUEST(1),
    OWNER(2),
    ADMIN(3);

    private final int value;

    UserRole(int value) {
        this.value = value;
    }

    public static UserRole fromValue(int value) {
        for (UserRole role : values()) {
            if (role.value == value) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role value: " + value);
    }
}