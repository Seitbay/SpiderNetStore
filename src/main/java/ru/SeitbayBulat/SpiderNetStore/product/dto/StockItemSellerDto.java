package ru.SeitbayBulat.SpiderNetStore.product.dto;

import lombok.Data;

@Data
public class StockItemSellerDto {
    private Long id;
    private String payloadType;
    private String status;
    private String originalFilename;
    private String dataPreview;
    private boolean hasBinary;
    private String createdAt;
}
