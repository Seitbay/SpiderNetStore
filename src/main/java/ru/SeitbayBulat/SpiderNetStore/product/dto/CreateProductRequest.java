package ru.SeitbayBulat.SpiderNetStore.product.dto;

import lombok.Data;
import ru.SeitbayBulat.SpiderNetStore.product.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private Integer stockCount;
    private ProductStatus status;
    private List<Long> categoryIds;
    private String fieldSchema;
}
