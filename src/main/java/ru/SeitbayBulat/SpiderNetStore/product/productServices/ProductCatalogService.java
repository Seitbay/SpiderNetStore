package ru.SeitbayBulat.SpiderNetStore.product.productServices;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.SeitbayBulat.SpiderNetStore.order.review.ReviewRepository;
import ru.SeitbayBulat.SpiderNetStore.product.Product;
import ru.SeitbayBulat.SpiderNetStore.product.ProductRepository;
import ru.SeitbayBulat.SpiderNetStore.product.ProductStatus;
import ru.SeitbayBulat.SpiderNetStore.product.category.Category;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductDetailDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductListDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductMapper;

@Service
@RequiredArgsConstructor
public class ProductCatalogService {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
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

        return productMapper.toListDto(products);
    }

    @Transactional(readOnly = true)
    public ProductDetailDto findById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        ProductDetailDto dto = new ProductDetailDto();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setRating(p.getRating());
        dto.setStockCount(p.getStockCount());
        dto.setStatus(p.getStatus().name());
        dto.setSellerUsername(p.getSeller().getUsername());
        dto.setSellerId(p.getSeller().getId());
        dto.setCategories(p.getCategories().stream()
                .map(Category::getName).toList());
        return dto;
    }

    @Transactional(readOnly = true)
    public ProductListDto findMyProducts(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Product> products = productRepository.findBySeller_Id(sellerId, pageable);
        return productMapper.toListDto(products);
    }
    /* todo Делаем делаем не забываем
    public ProductListDto findPopular(){

    }
    public ProductListDto findNewest(){

    }*/

}
