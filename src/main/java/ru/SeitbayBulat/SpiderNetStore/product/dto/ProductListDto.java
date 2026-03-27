package ru.SeitbayBulat.SpiderNetStore.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.SeitbayBulat.SpiderNetStore.product.Product;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListDto {
    private List<ProductDto> items;

    private int page;
    private int totalPages;
    private long totalElements;
}