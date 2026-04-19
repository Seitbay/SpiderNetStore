package ru.SeitbayBulat.SpiderNetStore.order.chat;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Id;
import ru.SeitbayBulat.SpiderNetStore.order.Order;
import ru.SeitbayBulat.SpiderNetStore.user.User;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;

    /** Мягкое удаление (фаза 5); {@code null} — активное сообщение */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}