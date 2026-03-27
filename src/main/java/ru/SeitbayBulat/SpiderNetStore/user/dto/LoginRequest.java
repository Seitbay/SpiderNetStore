package ru.SeitbayBulat.SpiderNetStore.user.dto;

import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}