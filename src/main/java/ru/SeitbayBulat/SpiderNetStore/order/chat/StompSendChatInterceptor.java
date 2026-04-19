package ru.SeitbayBulat.SpiderNetStore.order.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import ru.SeitbayBulat.SpiderNetStore.order.OrderRepository;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Разрешает клиентский SEND только на индикатор набора (фаза 4).
 */
@Component
@RequiredArgsConstructor
public class StompSendChatInterceptor implements ChannelInterceptor {

    private static final Pattern TYPING_DEST = Pattern.compile("^/app/orders/(\\d+)/chat/typing$");

    private final OrderRepository orderRepository;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        if (!StompCommand.SEND.equals(accessor.getCommand())) {
            return message;
        }
        String dest = accessor.getDestination();
        if (dest == null) {
            throw new AccessDeniedException("Нет destination");
        }
        Matcher m = TYPING_DEST.matcher(dest);
        if (!m.matches()) {
            throw new AccessDeniedException("Недопустимый destination");
        }
        long orderId = Long.parseLong(m.group(1));
        Principal user = accessor.getUser();
        if (!(user instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Не авторизован");
        }
        orderRepository.findByIdAndParticipant(orderId, principal.getId())
                .orElseThrow(() -> new AccessDeniedException("Нет доступа к чату заказа"));
        return message;
    }
}
