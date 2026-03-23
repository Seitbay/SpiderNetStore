package ru.SeitbayBulat.SpiderNetStore.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductDetailDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductListDto;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;  // final!

    @GetMapping
    public ResponseEntity<ProductListDto> getAllProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page) {
        int size = 20;

        return ResponseEntity.ok(
                productService.findAll(q, categoryId, page, size)
        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }
}