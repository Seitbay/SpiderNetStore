package ru.SeitbayBulat.SpiderNetStore.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.SeitbayBulat.SpiderNetStore.user.User;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByBuyerAndProductIdAndStatusAndReviewIsNull(
            User buyer, Long productId, OrderStatus status);
}
