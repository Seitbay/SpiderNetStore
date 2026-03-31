package ru.SeitbayBulat.SpiderNetStore.product.productServices;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.SeitbayBulat.SpiderNetStore.product.Product;
import ru.SeitbayBulat.SpiderNetStore.product.ProductRepository;
import ru.SeitbayBulat.SpiderNetStore.product.ProductStatus;
import ru.SeitbayBulat.SpiderNetStore.product.category.Category;
import ru.SeitbayBulat.SpiderNetStore.product.category.CategoryRepository;
import ru.SeitbayBulat.SpiderNetStore.product.dto.CreateProductRequest;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductManageDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ProductManageMapper;
import ru.SeitbayBulat.SpiderNetStore.product.dto.StockAppendDto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.UpdateProductRequest;
import ru.SeitbayBulat.SpiderNetStore.product.stock.StockService;
import ru.SeitbayBulat.SpiderNetStore.user.User;
import ru.SeitbayBulat.SpiderNetStore.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductManagementService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductStockImportService productStockImportService;
    private final StockService stockService;
    private final ProductManageMapper productManageMapper;

    @Transactional
    public ProductManageDto createProduct(Long sellerId, CreateProductRequest request) {
        User seller = loadSeller(sellerId);
        Product product = buildProduct(seller, request);
        product = productRepository.save(product);
        productStockImportService.importFromCreateRequest(product.getId(), request);
        return loadManageDto(product.getId(), sellerId);
    }

    @Transactional
    public ProductManageDto createProductWithFiles(Long sellerId, CreateProductRequest metadata,
                                                   List<MultipartFile> textFiles,
                                                   List<MultipartFile> jsonFiles,
                                                   List<MultipartFile> archiveFiles) {
        User seller = loadSeller(sellerId);
        Product product = buildProduct(seller, metadata);
        product = productRepository.save(product);
        Long pid = product.getId();
        productStockImportService.importTextFiles(pid, textFiles);
        productStockImportService.importJsonFiles(pid, jsonFiles);
        productStockImportService.importArchiveFiles(pid, archiveFiles);
        productStockImportService.importFromCreateRequest(pid, metadata);
        return loadManageDto(pid, sellerId);
    }

    @Transactional
    public ProductManageDto appendStockFromFiles(Long sellerId, Long productId,
                                                 List<MultipartFile> textFiles,
                                                 List<MultipartFile> jsonFiles,
                                                 List<MultipartFile> archiveFiles) {
        assertOwnsProduct(sellerId, productId);
        productStockImportService.importTextFiles(productId, textFiles);
        productStockImportService.importJsonFiles(productId, jsonFiles);
        productStockImportService.importArchiveFiles(productId, archiveFiles);
        return loadManageDto(productId, sellerId);
    }

    @Transactional
    public ProductManageDto appendStockBatch(Long sellerId, Long productId, StockAppendDto dto) {
        assertOwnsProduct(sellerId, productId);
        productStockImportService.importFromStockAppend(productId, dto);
        return loadManageDto(productId, sellerId);
    }

    @Transactional
    public ProductManageDto deleteStockItemForSeller(Long sellerId, Long productId, Long stockItemId) {
        assertOwnsProduct(sellerId, productId);
        stockService.deleteStockItem(productId, stockItemId);
        return loadManageDto(productId, sellerId);
    }

    @Transactional
    public ProductManageDto updateProduct(Long sellerId, Long productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Продукт не найден"));
        assertSeller(sellerId, product);

        if (request.getTitle() != null) {
            product.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getFieldSchema() != null) {
            product.setFieldSchema(request.getFieldSchema());
        }
        if (request.getCategoryIds() != null) {
            product.setCategories(resolveCategories(request.getCategoryIds()));
        }

        if (request.getDeleteStockItemIds() != null) {
            for (Long sid : request.getDeleteStockItemIds()) {
                if (sid != null) {
                    stockService.deleteStockItem(productId, sid);
                }
            }
        }

        productRepository.save(product);
        return loadManageDto(productId, sellerId);
    }

    @Transactional
    public void deleteProduct(Long sellerId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Продукт не найден"));
        assertSeller(sellerId, product);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public ProductManageDto getProductForManage(Long sellerId, Long productId) {
        return loadManageDto(productId, sellerId);
    }

    private ProductManageDto loadManageDto(Long productId, Long sellerId) {
        Product product = productRepository.findByIdWithCategoriesAndStock(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Продукт не найден"));
        assertSeller(sellerId, product);
        return productManageMapper.toManageDto(product);
    }

    private User loadSeller(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        if (!seller.canSell()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нужна роль продавца (SELLER)");
        }
        return seller;
    }

    private Product buildProduct(User seller, CreateProductRequest request) {
        Product product = new Product();
        product.setSeller(seller);
        product.setTitle(request.getTitle().trim());
        product.setDescription(request.getDescription() != null ? request.getDescription() : "");
        product.setPrice(request.getPrice());
        product.setStatus(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE);
        product.setStockCount(0);
        product.setFieldSchema(request.getFieldSchema());
        product.setCategories(resolveCategories(request.getCategoryIds()));
        return product;
    }

    private List<Category> resolveCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Category> list = categoryRepository.findAllByIdIn(categoryIds);
        if (list.size() != categoryIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Указана несуществующая категория");
        }
        return list;
    }

    private void assertOwnsProduct(Long sellerId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Продукт не найден"));
        assertSeller(sellerId, product);
    }

    private static void assertSeller(Long sellerId, Product product) {
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет прав на этот товар");
        }
    }
}
