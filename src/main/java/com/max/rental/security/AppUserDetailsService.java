package com.max.rental.security;

import com.max.rental.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Попытка аутентификации пользователя: {}", email);

        return userRepository.findByEmail(email)
                .map(user -> {
                    log.debug("Пользователь найден: {}, роли: {}",
                            user.getEmail(),
                            user.getRoles().stream()
                                    .map(r -> r.getName().name())
                                    .collect(Collectors.joining(", ")));

                    return new User(
                            user.getEmail(),
                            user.getPasswordHash(),
                            user.getRoles().stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                                    .collect(Collectors.toList())
                    );
                })
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: {}", email);
                    return new UsernameNotFoundException("Пользователь с email " + email + " не найден!");
                });
    }
}