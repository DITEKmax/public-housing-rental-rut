package com.max.rental.services.impl;

import com.max.rental.dto.auth.UserRegistrationDto;
import com.max.rental.models.entities.Role;
import com.max.rental.models.entities.User;
import com.max.rental.models.enums.UserRole;
import com.max.rental.repositories.RoleRepository;
import com.max.rental.repositories.UserRepository;
import com.max.rental.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(UserRegistrationDto dto) {
        log.info("Регистрация нового пользователя: {}", dto.getEmail());

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            log.warn("Попытка регистрации с существующим email: {}", dto.getEmail());
            throw new RuntimeException("Пользователь с таким email уже существует!");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Пароли не совпадают!");
        }

        UserRole userRole;
        try {
            userRole = UserRole.valueOf(dto.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            userRole = UserRole.GUEST;
        }

        final UserRole finalUserRole = userRole;

        Role role = roleRepository.findByName(finalUserRole)
                .orElseThrow(() -> new RuntimeException("Роль не найдена: " + finalUserRole));

        User user = new User(
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getFirstName(),
                dto.getLastName()
        );

        user.setPhone(dto.getPhone());
        user.setRoles(List.of(role));

        userRepository.save(user);

        log.info("Пользователь успешно зарегистрирован: {} с ролью {}",
                dto.getEmail(), finalUserRole);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}