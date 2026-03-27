package ru.SeitbayBulat.SpiderNetStore.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.SeitbayBulat.SpiderNetStore.user.User;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findTopByBuyerAndProduct_IdAndStatusAndReviewIsNullOrderByIdDesc(
            User buyer, Long productId, OrderStatus status);

    Page<Order> findByBuyer_IdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);

    Page<Order> findByProduct_Seller_IdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    Optional<Order> findByIdAndBuyer_Id(Long id, Long buyerId);

    Optional<Order> findByIdAndProduct_Seller_Id(Long id, Long sellerId);

    @Query("""
            SELECT o FROM Order o
            WHERE o.id = :orderId
            AND (o.buyer.id = :userId OR o.product.seller.id = :userId)
            """)
    Optional<Order> findByIdAndParticipant(@Param("orderId") Long orderId, @Param("userId") Long userId);
}
