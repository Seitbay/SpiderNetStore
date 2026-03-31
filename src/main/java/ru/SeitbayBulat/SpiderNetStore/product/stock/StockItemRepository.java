package ru.SeitbayBulat.SpiderNetStore.product.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    boolean existsByProductIdAndStatus(Long productId, StockItemStatus status);

    long countByProductIdAndStatus(Long productId, StockItemStatus status);

    Optional<StockItem> findFirstByProductIdAndStatusOrderByCreatedAtAsc(
            Long productId, StockItemStatus status);

    List<StockItem> findByProductIdAndStatus(Long productId, StockItemStatus status);

    List<StockItem> findByProduct_IdOrderByIdAsc(Long productId);

    Optional<StockItem> findByIdAndProduct_Id(Long stockItemId, Long productId);
}
