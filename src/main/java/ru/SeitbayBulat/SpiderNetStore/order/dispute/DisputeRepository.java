package ru.SeitbayBulat.SpiderNetStore.order.dispute;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    Optional<Dispute> findByOrder_Id(Long orderId);

    @Query("""
            SELECT d FROM Dispute d
            WHERE d.id = :disputeId
            AND (d.order.buyer.id = :userId OR d.order.product.seller.id = :userId)
            """)
    Optional<Dispute> findByIdAndParticipant(@Param("disputeId") Long disputeId, @Param("userId") Long userId);

    Optional<Dispute> findByIdAndOrder_Product_Seller_Id(Long disputeId, Long sellerId);
}
