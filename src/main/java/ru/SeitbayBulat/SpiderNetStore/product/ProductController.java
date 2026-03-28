package ru.SeitbayBulat.SpiderNetStore.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.SeitbayBulat.SpiderNetStore.product.dto.CreateProductRequest;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductDetailDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductListDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.UpdateProductRequest;
import ru.SeitbayBulat.SpiderNetStore.product.productServices.ProductCatalogService;
import ru.SeitbayBulat.SpiderNetStore.product.productServices.ProductManagementService;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCatalogService productCatalogService;
    private final ProductManagementService productManagementService;

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
    @GetMapping("/my")
    public ResponseEntity<ProductListDto> getMyProducts(
            @RequestParam(defaultValue = "0") int page) {
        int size = 20;
        Long sellerId = getCurrentUserId();
        return ResponseEntity.ok(productCatalogService.findMyProducts(sellerId, page, size));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest request) {
        Long sellerId = getCurrentUserId();
        Product product = productManagementService.createProduct(sellerId, request);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody UpdateProductRequest request) {
        Long sellerId = getCurrentUserId();
        Product product = productManagementService.updateProduct(sellerId, id, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Long sellerId = getCurrentUserId();
        productManagementService.deleteProduct(sellerId, id);
        return ResponseEntity.noContent().build();
    }


    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        return principal.getId();
    }
}