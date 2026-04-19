package ru.SeitbayBulat.SpiderNetStore.order.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = {"sender", "order", "order.buyer", "order.product", "order.product.seller"})
    Optional<ChatMessage> findByIdAndOrder_Id(Long id, Long orderId);

    @EntityGraph(attributePaths = "sender")
    List<ChatMessage> findByOrder_IdOrderByIdDesc(Long orderId, Pageable pageable);

    @EntityGraph(attributePaths = "sender")
    List<ChatMessage> findByOrder_IdAndIdLessThanOrderByIdDesc(Long orderId, Long id, Pageable pageable);

    @EntityGraph(attributePaths = "sender")
    List<ChatMessage> findByOrder_IdAndIdGreaterThanOrderByIdAsc(Long orderId, Long id, Pageable pageable);

    boolean existsByOrder_IdAndIdLessThan(Long orderId, Long id);

    @Query("""
            SELECT m FROM ChatMessage m
            WHERE m.order.id = :orderId
            AND m.sender.id <> :readerId
            AND m.deletedAt IS NULL
            AND NOT EXISTS (
                SELECT 1 FROM ChatMessageReadReceipt r
                WHERE r.message.id = m.id AND r.reader.id = :readerId
            )
            """)
    @EntityGraph(attributePaths = "sender")
    List<ChatMessage> findIncomingWithoutReceipt(@Param("orderId") Long orderId, @Param("readerId") Long readerId);

    @Query("""
            SELECT m.order.id, COUNT(m)
            FROM ChatMessage m
            WHERE (m.order.buyer.id = :userId OR m.order.product.seller.id = :userId)
            AND m.sender.id <> :userId
            AND m.deletedAt IS NULL
            AND NOT EXISTS (
                SELECT 1 FROM ChatMessageReadReceipt r
                WHERE r.message.id = m.id AND r.reader.id = :userId
            )
            GROUP BY m.order.id
            """)
    List<Object[]> countUnreadIncomingGroupedByOrder(@Param("userId") Long userId);
}
