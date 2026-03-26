package ru.SeitbayBulat.SpiderNetStore.order.dispute;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Id;
import ru.SeitbayBulat.SpiderNetStore.order.Order;
import ru.SeitbayBulat.SpiderNetStore.user.User;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "disputes")
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DisputeStatus status = DisputeStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String sellerResponse;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @ManyToOne
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}