package com.max.rental.services;

import com.max.rental.dto.auth.UserRegistrationDto;
import com.max.rental.models.entities.User;

public interface AuthService {


    void register(UserRegistrationDto registrationDto);


    User getUserByEmail(String email);


    boolean emailExists(String email);
}