package ru.SeitbayBulat.SpiderNetStore.order.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ChatTypingClientPayload;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.security.Principal;

/**
 * Входящие STOMP-сообщения приложения (фаза 4: индикатор набора).
 */
@Controller
@RequiredArgsConstructor
public class OrderChatStompController {

    private final OrderChatRealtimePublisher orderChatRealtimePublisher;

    @MessageMapping("/orders/{orderId}/chat/typing")
    public void typing(
            @DestinationVariable Long orderId,
            @Payload(required = false) ChatTypingClientPayload payload,
            Principal principal
    ) {
        if (!(principal instanceof UserPrincipal user)) {
            return;
        }
        boolean typing = payload != null && payload.isTyping();
        orderChatRealtimePublisher.publishTyping(orderId, user.getId(), typing);
    }
}
