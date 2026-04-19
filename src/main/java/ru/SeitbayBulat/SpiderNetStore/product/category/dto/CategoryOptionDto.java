package ru.SeitbayBulat.SpiderNetStore.product.category.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryOptionDto {
    private Long id;
    private String slug;
    private String name;
}
