package ru.SeitbayBulat.SpiderNetStore.order.chat;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Пробрасывает {@link ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal} из атрибутов handshake в STOMP-сессию.
 */
public class ChatPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        Object u = attributes.get(JwtHandshakeInterceptor.USER_ATTR);
        return u instanceof Principal p ? p : null;
    }
}
