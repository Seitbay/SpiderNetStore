package ru.SeitbayBulat.SpiderNetStore.order.dto;

import lombok.Data;

@Data
public class DisputeDto {
    private Long id;
    private Long orderId;
    private String status;
    private String reason;
    private String sellerResponse;
    private String resolution;
    private Long initiatorId;
    private String initiatorUsername;
    private Long resolvedById;
    private String resolvedByUsername;
    private String createdAt;
    private String resolvedAt;
}
