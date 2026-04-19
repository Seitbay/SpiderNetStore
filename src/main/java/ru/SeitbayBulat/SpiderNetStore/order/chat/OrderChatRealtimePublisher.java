package ru.SeitbayBulat.SpiderNetStore.order.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ChatMessageDto;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ChatRealtimeEnvelope;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderChatRealtimePublisher {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SimpMessagingTemplate messagingTemplate;

    public void publishNewMessage(long orderId, ChatMessageDto message) {
        ChatRealtimeEnvelope env = ChatRealtimeEnvelope.builder()
                .type(ChatRealtimeEnvelope.TYPE_NEW_MESSAGE)
                .message(message)
                .build();
        messagingTemplate.convertAndSend(destination(orderId), env);
    }

    public void publishReadReceipt(long orderId, long readerId, List<Long> messageIds, LocalDateTime readAt) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }
        ChatRealtimeEnvelope.ChatReadReceiptPayload read = ChatRealtimeEnvelope.ChatReadReceiptPayload.builder()
                .readerId(readerId)
                .messageIds(messageIds)
                .readAt(readAt != null ? ISO.format(readAt) : null)
                .build();
        ChatRealtimeEnvelope env = ChatRealtimeEnvelope.builder()
                .type(ChatRealtimeEnvelope.TYPE_READ_RECEIPT)
                .read(read)
                .build();
        messagingTemplate.convertAndSend(destination(orderId), env);
    }

    public void publishTyping(long orderId, long userId, boolean typing) {
        ChatRealtimeEnvelope.ChatTypingPayload payload = ChatRealtimeEnvelope.ChatTypingPayload.builder()
                .userId(userId)
                .typing(typing)
                .build();
        ChatRealtimeEnvelope env = ChatRealtimeEnvelope.builder()
                .type(ChatRealtimeEnvelope.TYPE_TYPING)
                .typing(payload)
                .build();
        messagingTemplate.convertAndSend(destination(orderId), env);
    }

    public void publishMessageDeleted(long orderId, ChatMessageDto message) {
        ChatRealtimeEnvelope env = ChatRealtimeEnvelope.builder()
                .type(ChatRealtimeEnvelope.TYPE_MESSAGE_DELETED)
                .message(message)
                .build();
        messagingTemplate.convertAndSend(destination(orderId), env);
    }

    private static String destination(long orderId) {
        return "/topic/orders/" + orderId + "/chat";
    }
}
