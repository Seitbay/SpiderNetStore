package ru.SeitbayBulat.SpiderNetStore.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            SELECT t FROM Transaction t
            WHERE (t.fromUser IS NOT NULL AND t.fromUser.id = :userId)
               OR (t.toUser IS NOT NULL AND t.toUser.id = :userId)
            ORDER BY t.createdAt DESC
            """)
    Page<Transaction> findByUserInvolved(@Param("userId") Long userId, Pageable pageable);
}
