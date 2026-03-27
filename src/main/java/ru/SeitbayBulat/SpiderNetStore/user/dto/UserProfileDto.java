package ru.SeitbayBulat.SpiderNetStore.user.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserProfileDto {
    private Long id;
    private String email;
    private String username;
    private BigDecimal balance;
    private String role;
    private boolean isVerified;
}