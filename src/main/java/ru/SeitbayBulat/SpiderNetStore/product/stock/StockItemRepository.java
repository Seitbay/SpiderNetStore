package ru.SeitbayBulat.SpiderNetStore.product.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.SeitbayBulat.SpiderNetStore.product.Product;
import ru.SeitbayBulat.SpiderNetStore.product.stock.StockItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    boolean existsByProductIdAndStatus(Long productId, StockItemStatus status);

    long countByProductIdAndStatus(Long productId, StockItemStatus status);

    Optional<StockItem> findFirstByProductIdAndStatusOrderByCreatedAtAsc(
            Long productId, StockItemStatus status);

    // Если захочешь массово добавлять
    List<StockItem> findByProductIdAndStatus(Long productId, StockItemStatus status);
}