package ru.SeitbayBulat.SpiderNetStore.product.stock;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.Id;
import ru.SeitbayBulat.SpiderNetStore.product.Product;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "stock_items")
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockItemStatus status = StockItemStatus.AVAILABLE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}