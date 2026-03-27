package ru.SeitbayBulat.SpiderNetStore.order.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Long id;
    private Integer rating;
    private String comment;
    private String buyerUsername;
    private String createdAt;
}