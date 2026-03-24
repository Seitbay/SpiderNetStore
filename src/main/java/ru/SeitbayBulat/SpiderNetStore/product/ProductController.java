package ru.SeitbayBulat.SpiderNetStore.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductDetailDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductListDto;
import ru.SeitbayBulat.SpiderNetStore.product.productServices.ProductCatalogService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCatalogService productCatalogService;

    @GetMapping
    public ResponseEntity<ProductListDto> getAllProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page) {
        int size = 20;

        return ResponseEntity.ok(
                productCatalogService.findAll(q, categoryId, page, size)
        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(productCatalogService.findById(id));
    }
}