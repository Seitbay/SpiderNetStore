package ru.SeitbayBulat.SpiderNetStore.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.processing.Pattern;

@Data
public class UpdateProfileRequest {
    @Size(min = 3, max = 30, message = "Username от 3 до 30 символов")
    private String username;
    // private String email;
    // private String avatar;    // позже
}