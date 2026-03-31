package ru.SeitbayBulat.SpiderNetStore.order.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Long id;
    private Long buyerId;
    private String buyerUsername;
    private Integer rating;
    private String comment;
    private String createdAt;
}
