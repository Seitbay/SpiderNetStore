package ru.SeitbayBulat.SpiderNetStore.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Событие чата заказа по WebSocket (STOMP broadcast).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRealtimeEnvelope {
 
    public static final String TYPE_NEW_MESSAGE = "NEW_MESSAGE";
    public static final String TYPE_READ_RECEIPT = "READ_RECEIPT";
    public static final String TYPE_TYPING = "TYPING";
    public static final String TYPE_MESSAGE_DELETED = "MESSAGE_DELETED";

    /** Тип события (см. константы {@code TYPE_*}). */
    private String type;
    private ChatMessageDto message;
    private ChatReadReceiptPayload read;
    private ChatTypingPayload typing;
 
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatReadReceiptPayload {
        private Long readerId;
        private List<Long> messageIds;
        /** ISO-8601 */
        private String readAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatTypingPayload {
        private Long userId;
        private boolean typing;
    }
}