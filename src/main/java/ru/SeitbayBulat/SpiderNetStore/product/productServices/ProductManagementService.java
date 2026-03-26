package ru.SeitbayBulat.SpiderNetStore.product.productServices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.SeitbayBulat.SpiderNetStore.product.Product;
import ru.SeitbayBulat.SpiderNetStore.product.ProductRepository;
import ru.SeitbayBulat.SpiderNetStore.product.ProductStatus;
import ru.SeitbayBulat.SpiderNetStore.product.dto.CreateProductRequest;
import ru.SeitbayBulat.SpiderNetStore.product.dto.UpdateProductRequest;
import ru.SeitbayBulat.SpiderNetStore.user.User;
import ru.SeitbayBulat.SpiderNetStore.user.UserRepository;


@Service
@RequiredArgsConstructor
public class ProductManagementService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ======================= CREATE =======================
    @Transactional
    public Product createProduct(Long sellerId, CreateProductRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Продавец не найден"));

        Product product = new Product();
        product.setSeller(seller);
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStatus(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE);
        product.setStockCount(request.getStockCount() != null ? request.getStockCount() : 0);
        // TODO: Добавь категории, fieldSchema, другие поля если нужно

        return productRepository.save(product);
    }

    // ======================= UPDATE =======================
    @Transactional
    public Product updateProduct(Long sellerId, Long productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Нет прав на редактирование продукта");
        }

        if (request.getTitle() != null) product.setTitle(request.getTitle());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getStockCount() != null) product.setStockCount(request.getStockCount());
        // TODO: обновление категорий, fieldSchema и других полей

        return productRepository.save(product);
    }

    // ======================= DELETE =======================
    @Transactional
    public void deleteProduct(Long sellerId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Нет прав на удаление продукта");
        }

        productRepository.delete(product);
    }
}