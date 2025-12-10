package com.max.rental.controllers;

import com.max.rental.dto.auth.UserLoginDto;
import com.max.rental.dto.auth.UserRegistrationDto;
import com.max.rental.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("loginDto")) {
            model.addAttribute("loginDto", new UserLoginDto());
        }
        return "auth/login";
    }


    @PostMapping("/login-error")
    public String loginError(Model model, RedirectAttributes redirectAttributes) {
        log.warn("Неудачная попытка входа");
        redirectAttributes.addFlashAttribute("errorMessage", "Неверный email или пароль!");
        return "redirect:/auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registrationDto")) {
            model.addAttribute("registrationDto", new UserRegistrationDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationDto") UserRegistrationDto dto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {

        log.info("Попытка регистрации пользователя: {}", dto.getEmail());

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                    "Пароли не совпадают!");
        }

        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при регистрации: {}", bindingResult.getAllErrors());
            return "auth/register";
        }

        try {
            authService.register(dto);
            log.info("Пользователь успешно зарегистрирован: {}", dto.getEmail());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Регистрация успешна! Теперь вы можете войти.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            log.error("Ошибка регистрации: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/register";
        }
    }


    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }
}