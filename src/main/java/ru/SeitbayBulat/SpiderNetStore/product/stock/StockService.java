package ru.SeitbayBulat.SpiderNetStore.product.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.SeitbayBulat.SpiderNetStore.product.Product;
import ru.SeitbayBulat.SpiderNetStore.product.ProductRepository;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public boolean hasAvailableStock(Long productId) {
        return stockItemRepository.existsByProductIdAndStatus(
                productId, StockItemStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public long getAvailableStockCount(Long productId) {
        return stockItemRepository.countByProductIdAndStatus(
                productId, StockItemStatus.AVAILABLE);
    }

    @Transactional
    public StockItem reserveStockItem(Long productId) {
        StockItem stockItem = stockItemRepository
                .findFirstByProductIdAndStatusOrderByCreatedAtAsc(
                        productId, StockItemStatus.AVAILABLE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Товар закончился"));

        stockItem.setStatus(StockItemStatus.RESERVED);
        stockItemRepository.save(stockItem);

        updateProductStockCount(productId);

        return stockItem;
    }

    @Transactional
    public void confirmSale(StockItem stockItem) {
        stockItem.setStatus(StockItemStatus.SOLD);
        stockItemRepository.save(stockItem);

        updateProductStockCount(stockItem.getProduct().getId());
    }

    @Transactional
    public void releaseStockItem(StockItem stockItem) {
        stockItem.setStatus(StockItemStatus.AVAILABLE);
        stockItemRepository.save(stockItem);

        updateProductStockCount(stockItem.getProduct().getId());
    }

    /**
     * Простое JSON-значение (как раньше) — трактуем как {@link StockPayloadType#JSON_OBJECT}.
     */
    @Transactional
    public StockItem addStockItem(Long productId, String dataJson) {
        return addStockItem(productId, StockPayloadType.JSON_OBJECT, dataJson, null, null);
    }

    @Transactional
    public StockItem addStockItem(Long productId, StockPayloadType payloadType, String dataJson,
                                    byte[] binaryPayload, String originalFilename) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден"));

        StockItem stockItem = new StockItem();
        stockItem.setProduct(product);
        stockItem.setPayloadType(payloadType);
        stockItem.setData(dataJson);
        stockItem.setBinaryPayload(binaryPayload);
        stockItem.setOriginalFilename(originalFilename);
        stockItem.setStatus(StockItemStatus.AVAILABLE);

        stockItemRepository.save(stockItem);
        updateProductStockCount(productId);

        return stockItem;
    }

    @Transactional
    public void deleteStockItem(Long productId, Long stockItemId) {
        StockItem s = stockItemRepository.findByIdAndProduct_Id(stockItemId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Позиция склада не найдена"));
        if (s.getStatus() != StockItemStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Можно удалить только свободную позицию (не зарезервированную и не проданную)");
        }
        stockItemRepository.delete(s);
        updateProductStockCount(productId);
    }

    private void updateProductStockCount(Long productId) {
        long count = getAvailableStockCount(productId);
        productRepository.updateStockCount(productId, (int) count);
    }
}
