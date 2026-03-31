package ru.SeitbayBulat.SpiderNetStore.product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.SeitbayBulat.SpiderNetStore.product.dto.CreateProductRequest;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductDetailDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductListDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductManageDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.StockAppendDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.UpdateProductRequest;
import ru.SeitbayBulat.SpiderNetStore.product.productServices.ProductCatalogService;
import ru.SeitbayBulat.SpiderNetStore.product.productServices.ProductManagementService;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductCatalogService productCatalogService;
    private final ProductManagementService productManagementService;

    @GetMapping
    public ResponseEntity<ProductListDto> getAllProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productCatalogService.findAll(q, categoryId, page, clamp(size)));
    }

    @GetMapping("/my")
    public ResponseEntity<ProductListDto> getMyProducts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(productCatalogService.findMyProducts(principal.getId(), page, clamp(size)));
    }

    @GetMapping("/{id}/manage")
    public ResponseEntity<ProductManageDto> getManage(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(productManagementService.getProductForManage(principal.getId(), id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(productCatalogService.findById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductManageDto> createProductJson(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(productManagementService.createProduct(principal.getId(), request));
    }

    @PostMapping(value = "/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductManageDto> createProductWithFiles(
            @Valid @RequestPart("metadata") CreateProductRequest metadata,
            @RequestPart(value = "textFiles", required = false) List<MultipartFile> textFiles,
            @RequestPart(value = "jsonFiles", required = false) List<MultipartFile> jsonFiles,
            @RequestPart(value = "archiveFiles", required = false) List<MultipartFile> archiveFiles,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(productManagementService.createProductWithFiles(
                principal.getId(), metadata, textFiles, jsonFiles, archiveFiles));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductManageDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(productManagementService.updateProduct(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        productManagementService.deleteProduct(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/stock/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductManageDto> appendStockFiles(
            @PathVariable Long id,
            @RequestPart(value = "textFiles", required = false) List<MultipartFile> textFiles,
            @RequestPart(value = "jsonFiles", required = false) List<MultipartFile> jsonFiles,
            @RequestPart(value = "archiveFiles", required = false) List<MultipartFile> archiveFiles,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(productManagementService.appendStockFromFiles(
                principal.getId(), id, textFiles, jsonFiles, archiveFiles));
    }

    @PostMapping(value = "/{id}/stock", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductManageDto> appendStockJson(
            @PathVariable Long id,
            @Valid @RequestBody StockAppendDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(productManagementService.appendStockBatch(principal.getId(), id, dto));
    }

    @DeleteMapping("/{id}/stock/{stockItemId}")
    public ResponseEntity<?> deleteStockItem(
            @PathVariable Long id,
            @PathVariable Long stockItemId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Не авторизован"));
        }
        return ResponseEntity.ok(productManagementService.deleteStockItemForSeller(principal.getId(), id, stockItemId));
    }

    private static int clamp(int size) {
        if (size < 1) {
            return 1;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
