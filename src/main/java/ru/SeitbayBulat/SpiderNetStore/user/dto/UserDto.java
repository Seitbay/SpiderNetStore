package ru.SeitbayBulat.SpiderNetStore.user.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserDto {
    private String email;
    private String username;
    private BigDecimal balance;
    private String role;
}