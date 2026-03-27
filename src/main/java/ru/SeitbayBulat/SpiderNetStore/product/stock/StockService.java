package ru.SeitbayBulat.SpiderNetStore.product.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.SeitbayBulat.SpiderNetStore.product.Product;
import ru.SeitbayBulat.SpiderNetStore.product.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final ProductRepository productRepository;   // понадобится для обновления stockCount

    /**
     * Проверяет, есть ли в наличии хотя бы один AVAILABLE StockItem
     */
    @Transactional(readOnly = true)
    public boolean hasAvailableStock(Long productId) {
        return stockItemRepository.existsByProductIdAndStatus(
                productId, StockItemStatus.AVAILABLE);
    }

    /**
     * Возвращает точное количество доступных items
     */
    @Transactional(readOnly = true)
    public long getAvailableStockCount(Long productId) {
        return stockItemRepository.countByProductIdAndStatus(
                productId, StockItemStatus.AVAILABLE);
    }

    /**
     * Резервирует один StockItem при покупке (меняет статус на RESERVED)
     * Возвращает зарезервированный StockItem
     */
    @Transactional
    public StockItem reserveStockItem(Long productId) {
        StockItem stockItem = stockItemRepository
                .findFirstByProductIdAndStatusOrderByCreatedAtAsc(
                        productId, StockItemStatus.AVAILABLE)
                .orElseThrow(() -> new RuntimeException("Товар закончился"));

        stockItem.setStatus(StockItemStatus.RESERVED);
        stockItemRepository.save(stockItem);

        updateProductStockCount(productId);

        return stockItem;
    }

    /**
     * Подтверждает продажу — меняет статус на SOLD
     */
    @Transactional
    public void confirmSale(StockItem stockItem) {
        stockItem.setStatus(StockItemStatus.SOLD);
        stockItemRepository.save(stockItem);

        updateProductStockCount(stockItem.getProduct().getId());
    }

    /**
     * Возвращает товар в доступные (например, при отмене заказа)
     */
    @Transactional
    public void releaseStockItem(StockItem stockItem) {
        stockItem.setStatus(StockItemStatus.AVAILABLE);
        stockItemRepository.save(stockItem);

        updateProductStockCount(stockItem.getProduct().getId());
    }

    /**
     * Добавляет новый StockItem в товар
     */
    @Transactional
    public StockItem addStockItem(Long productId, String data) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        StockItem stockItem = new StockItem();
        stockItem.setProduct(product);
        stockItem.setData(data);
        stockItem.setStatus(StockItemStatus.AVAILABLE);

        stockItemRepository.save(stockItem);
        updateProductStockCount(productId);

        return stockItem;
    }

    /**
     * Внутренний метод для синхронизации stockCount в Product
     */
    private void updateProductStockCount(Long productId) {
        long count = getAvailableStockCount(productId);
        productRepository.updateStockCount(productId, (int) count);
    }
}