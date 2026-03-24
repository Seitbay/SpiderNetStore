package ru.SeitbayBulat.SpiderNetStore.order.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.SeitbayBulat.SpiderNetStore.product.Product;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
    @Query("""
        SELECT r FROM Review r
        JOIN FETCH r.buyer
        WHERE r.product.id = :productId
    """)
    List<Review> findByProductIdWithBuyer(Long productId);
}
