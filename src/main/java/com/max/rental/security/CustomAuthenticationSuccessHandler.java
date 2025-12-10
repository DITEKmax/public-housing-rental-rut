package com.max.rental.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        log.info("Успешная аутентификация пользователя: {}", username);
        log.info("Роли пользователя: {}", authorities);

        String redirectUrl = determineTargetUrl(authorities);

        log.info("Перенаправление на: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private String determineTargetUrl(Collection<? extends GrantedAuthority> authorities) {

        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                return "/admin";
            }
        }

        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_OWNER")) {
                return "/owner/listings";
            }
        }

        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_GUEST")) {
                return "/listings";
            }
        }

        return "/";
    }
}