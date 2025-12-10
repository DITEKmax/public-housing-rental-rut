package com.max.rental.dto.profile;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateDto {

    @Size(min = 2, max = 50, message = "Имя должно быть от 2 до 50 символов!")
    private String firstName;

    @Size(min = 2, max = 50, message = "Фамилия должна быть от 2 до 50 символов!")
    private String lastName;

    @Pattern(
            regexp = "^$|^(\\+7|8)[\\s\\-]?\\(?\\d{3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}$",
            message = "Введите корректный номер телефона (например, +7 999 123-45-67 или 89991234567)"
    )
    private String phone;
}