package ru.SeitbayBulat.SpiderNetStore.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.SeitbayBulat.SpiderNetStore.product.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateProductRequest {
    private String title;
    private String description;

    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal price;

    private ProductStatus status;
    private List<Long> categoryIds;
    private String fieldSchema;

    @Size(max = 2048)
    private String imageUrl;

    /** Удалить только свободные (AVAILABLE) позиции склада. */
    private List<Long> deleteStockItemIds;
}
