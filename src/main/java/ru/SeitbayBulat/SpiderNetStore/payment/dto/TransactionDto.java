package ru.SeitbayBulat.SpiderNetStore.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDto {
    private Long id;
    private String type;
    private BigDecimal amount;
    private String createdAt;

    private Long orderId;

    private Long fromUserId;
    private String fromUsername;

    private Long toUserId;
    private String toUsername;
}
