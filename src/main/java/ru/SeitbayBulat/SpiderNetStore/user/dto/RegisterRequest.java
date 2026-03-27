package ru.SeitbayBulat.SpiderNetStore.user.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    private String email;

    @NotBlank(message = "Username обязателен")
    @Size(min = 3, max = 30, message = "Username от 3 до 30 символов")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль минимум 6 символов")
    private String password;
}