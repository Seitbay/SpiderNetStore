package ru.SeitbayBulat.SpiderNetStore.product.dto;

import lombok.Data;
import ru.SeitbayBulat.SpiderNetStore.product.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal rating;
    private Integer stockCount;
    private ProductStatus status;

    private List<String> categories;
}