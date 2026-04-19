package ru.SeitbayBulat.SpiderNetStore.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OpenDisputeRequest {
    @NotBlank
    private String reason;
}
