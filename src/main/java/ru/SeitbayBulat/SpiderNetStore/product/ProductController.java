package ru.SeitbayBulat.SpiderNetStore.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductListDto;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    @GetMapping("/")
    public ResponseEntity<ProductListDto> getAllProducts()
}
