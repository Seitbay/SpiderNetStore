package ru.SeitbayBulat.SpiderNetStore.order;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.Id;
import ru.SeitbayBulat.SpiderNetStore.order.chat.ChatMessage;
import ru.SeitbayBulat.SpiderNetStore.order.dispute.Dispute;
import ru.SeitbayBulat.SpiderNetStore.order.review.Review;
import ru.SeitbayBulat.SpiderNetStore.product.Product;
import ru.SeitbayBulat.SpiderNetStore.product.stock.StockItem;
import ru.SeitbayBulat.SpiderNetStore.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToOne
    @JoinColumn(name = "stock_item_id")
    private StockItem stockItem;

    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Review review;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Dispute dispute;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<ChatMessage> messages = new ArrayList<>();
}