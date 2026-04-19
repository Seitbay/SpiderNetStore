package ru.SeitbayBulat.SpiderNetStore.order.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import ru.SeitbayBulat.SpiderNetStore.user.User;

import java.time.LocalDateTime;

/**
 * Квитанция: пользователь {@code reader} прочитал сообщение {@code message}.
 * Уникальность (message, reader) — один акт прочтения на пару.
 */
@Data
@Entity
@Table(
        name = "chat_message_read_receipts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_receipt_message_reader",
                columnNames = {"message_id", "reader_id"}
        )
)
public class ChatMessageReadReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessage message;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reader_id", nullable = false)
    private User reader;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;
}
