package ru.SeitbayBulat.SpiderNetStore.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.SeitbayBulat.SpiderNetStore.product.category.Category;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductListDto;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductListDto findAll(String q, Long categoryId, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Product> products;

        if (q != null && !q.isBlank()) {
            products = productRepository.search(q, pageable);
        } else if (categoryId != null) {
            products = productRepository.findAllByStatusAndCategories_Id(
                    ProductStatus.ACTIVE, categoryId, pageable);
        } else {
            products = productRepository.findAllByStatus(
                    ProductStatus.ACTIVE, pageable);
        }

        return toListDto(products);
    }

    private ProductListDto toListDto(Page<Product> products) {

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

    private ProductDto toDto(Product p) {

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
}