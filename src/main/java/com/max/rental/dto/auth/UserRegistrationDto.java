package com.max.rental.dto.auth;

import com.max.rental.validation.UniqueEmail;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.Value;


@Data
public class UserRegistrationDto {

    @NotEmpty(message = "Email не должен быть пустым!")
    @Email(message = "Введите корректный email!")
    @UniqueEmail
    private String email;

    @NotEmpty(message = "Имя не должно быть пустым!")
    @Size(min = 2, max = 50, message = "Имя должно быть от 2 до 50 символов!")
    private String firstName;

    @NotEmpty(message = "Фамилия не должна быть пустой!")
    @Size(min = 2, max = 50, message = "Фамилия должна быть от 2 до 50 символов!")
    private String lastName;

    @NotEmpty(message = "Пароль не должен быть пустым!")
    @Size(min = 8, max = 100, message = "Пароль должен быть от 6 до 100 символов!")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d].{7,}$",
            message = "Пароль должен содержать: минимум 8 символов, заглавные и строчные буквы, и цифры!")
    private String password;

    @NotEmpty(message = "Подтверждение пароля не должно быть пустым!")
    private String confirmPassword;

    @Pattern(
            regexp = "^$|^(\\+7|8)[\\s\\-]?\\(?\\d{3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}$",
            message = "Введите корректный номер телефона (например, +7 999 123-45-67 или 89991234567)"
    )
    private String phone;

    @NotNull(message = "Выберите тип аккаунта!")
    private String role = "GUEST";
}