package ru.SeitbayBulat.SpiderNetStore.payment;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import ru.SeitbayBulat.SpiderNetStore.order.Order;
import ru.SeitbayBulat.SpiderNetStore.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;                                // null для DEPOSIT и PAYOUT

    @ManyToOne
    @JoinColumn(name = "from_user_id")
    private User fromUser;                              // кто платит (null при пополнении)

    @ManyToOne
    @JoinColumn(name = "to_user_id")
    private User toUser;                                // кто получает (null при выводе)

    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
