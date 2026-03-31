package ru.SeitbayBulat.SpiderNetStore.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotNull
    private Long productId;
}
