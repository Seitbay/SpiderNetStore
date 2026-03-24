package ru.SeitbayBulat.SpiderNetStore.product.dto;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ReviewDto;
import ru.SeitbayBulat.SpiderNetStore.order.review.Review;
import ru.SeitbayBulat.SpiderNetStore.product.Product;
import ru.SeitbayBulat.SpiderNetStore.product.category.Category;

@Component
public class ProductMapper {
    public ProductListDto toListDto(Page<Product> products) {

        ProductListDto dto = new ProductListDto();

        dto.setItems(
                products.getContent()
                        .stream()
                        .map(this::toDto)
                        .toList()
        );

        dto.setPage(products.getNumber());
        dto.setTotalPages(products.getTotalPages());
        dto.setTotalElements(products.getTotalElements());

        return dto;
    }

    public ProductDto toDto(Product p) {

        ProductDto dto = new ProductDto();

        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setPrice(p.getPrice());
        dto.setRating(p.getRating());
        dto.setStockCount(p.getStockCount());

        dto.setCategories(
                p.getCategories().stream()
                        .map(Category::getName)
                        .toList()
        );

        return dto;
    }
    public ReviewDto toReviewDto(Review r) {
        ReviewDto dto = new ReviewDto();
        dto.setId(r.getId());
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setBuyerUsername(r.getBuyer().getUsername());
        dto.setCreatedAt(r.getCreatedAt().toString());
        return dto;
    }
}
