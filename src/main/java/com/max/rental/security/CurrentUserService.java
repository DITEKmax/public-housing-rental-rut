package com.max.rental.security;

import com.max.rental.models.entities.User;
import com.max.rental.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Попытка получить текущего пользователя без аутентификации");
            throw new UsernameNotFoundException("Пользователь не аутентифицирован");
        }

        String email = authentication.getName();
        log.debug("Получение пользователя по email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден в БД: {}", email);
                    return new UsernameNotFoundException("Пользователь не найден: " + email);
                });
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    public Optional<User> getCurrentUserOptional() {
        try {
            return Optional.of(getCurrentUser());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean isCurrentUserOwner() {
        try {
            return getCurrentUser().isOwner();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCurrentUserAdmin() {
        try {
            return getCurrentUser().isAdmin();
        } catch (Exception e) {
            return false;
        }
    }
}