package ru.SeitbayBulat.SpiderNetStore.payment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    @DecimalMax("999999.99")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;
}
