package com.max.rental.dto.auth;

import lombok.Data;

@Data
public class UserLoginDto {
    private String username;
    private String password;
    private boolean rememberMe;
}