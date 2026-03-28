package ru.SeitbayBulat.SpiderNetStore.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDto {
    private Long id;
    private String status;
    private BigDecimal amount;
    private String createdAt;

    private Long buyerId;
    private String buyerUsername;

    private Long sellerId;
    private String sellerUsername;

    private Long productId;
    private String productTitle;

    private Long stockItemId;
}
