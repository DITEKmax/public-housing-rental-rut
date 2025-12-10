package com.max.rental.config;

import com.max.rental.repositories.UserRepository;
import com.max.rental.security.AppUserDetailsService;
import com.max.rental.security.CustomAuthenticationSuccessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;


@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    private final UserRepository userRepository;
    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfiguration(UserRepository userRepository,
                                 CustomAuthenticationSuccessHandler successHandler) {
        this.userRepository = userRepository;
        this.successHandler = successHandler;
        log.info("SecurityConfiguration инициализирована");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SecurityContextRepository securityContextRepository) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/favicon.ico", "/error", "/css/**", "/js/**", "/images/**").permitAll()

                        .requestMatchers("/", "/listings", "/listings/{id}").permitAll()
                        .requestMatchers("/auth/login", "/auth/register", "/auth/login-error", "/auth/access-denied").permitAll()

                        .requestMatchers("/bookings/**").hasRole("GUEST")
                        .requestMatchers("/favorites/**").hasRole("GUEST")
                        .requestMatchers("/profile", "/profile/**").hasRole("GUEST")

                        .requestMatchers("/owner/**").hasRole("OWNER")

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureForwardUrl("/auth/login-error")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecretKeyForRentalApp2024")
                        .tokenValiditySeconds(86400 * 14) // 14 дней
                        .userDetailsService(userDetailsService())
                        .rememberMeParameter("remember-me")
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository)
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/auth/access-denied")
                );

        log.info("SecurityFilterChain настроен с ролями: GUEST, OWNER, ADMIN");
        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new AppUserDetailsService(userRepository);
    }
}