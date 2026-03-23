package ru.SeitbayBulat.SpiderNetStore.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ReviewDto;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal rating;
    private Integer stockCount;
    private String status;
    private String sellerUsername;
    private Long sellerId;
    private List<String> categories;
    private List<ReviewDto> reviews;
}
