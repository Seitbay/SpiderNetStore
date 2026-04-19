package ru.SeitbayBulat.SpiderNetStore.order.chat;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import ru.SeitbayBulat.SpiderNetStore.user.security.JwtUtil;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.util.Map;

/**
 * Проверяет JWT при WebSocket handshake (кука {@code jwt}, заголовок Authorization или query {@code access_token}).
 */
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    static final String USER_ATTR = "chatWsUser";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        HttpServletRequest req = servletRequest.getServletRequest();
        String token = extractToken(req);
        if (token == null || !jwtUtil.isTokenValid(token)) {
            return false;
        }
        String email = jwtUtil.extractEmail(token);
        final UserDetails details;
        try {
            details = userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            return false;
        }
        if (!(details instanceof UserPrincipal principal)) {
            return false;
        }
        attributes.put(USER_ATTR, principal);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }

    private static String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        String qp = request.getParameter("access_token");
        if (qp != null && !qp.isBlank()) {
            return qp.trim();
        }
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("jwt".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
