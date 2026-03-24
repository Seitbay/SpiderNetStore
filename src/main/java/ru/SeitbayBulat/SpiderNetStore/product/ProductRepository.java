package ru.SeitbayBulat.SpiderNetStore.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findAllByStatusAndCategories_Id(
            ProductStatus status, Long categoryId, Pageable pageable);

    @Query("""
        SELECT p FROM Product p 
        WHERE p.status = 'ACTIVE'
        AND (
            LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%')) 
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%'))
        )
    """)
    Page<Product> search(@Param("q") String q, Pageable pageable);

    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.categories
        WHERE p.status = :status
    """)
    Page<Product> findAllWithCategories(ProductStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.stockCount = :count WHERE p.id = :productId")
    void updateStockCount(@Param("productId") Long productId, @Param("count") int count);
}