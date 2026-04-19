package ru.SeitbayBulat.SpiderNetStore.order.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ChatMessageDto;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ChatMessagesPageDto;
import ru.SeitbayBulat.SpiderNetStore.order.dto.SendChatMessageRequest;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/orders/{orderId}/chat")
@RequiredArgsConstructor
public class OrderChatController {

    private final OrderChatService orderChatService;
    private final ChatMessageMapper chatMessageMapper;

    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable Long orderId,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(required = false) Long afterId,
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        ChatMessagesPageDto page = orderChatService.getMessagesPage(
                orderId, principal.getId(), beforeId, afterId, limit);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@PathVariable Long orderId,
                                         @Valid @RequestBody SendChatMessageRequest req,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        ChatMessage message = orderChatService.sendMessage(orderId, principal.getId(), req.getText());
        ChatMessageDto dto = chatMessageMapper.toDtoJustPosted(message);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/read")
    public ResponseEntity<?> markRead(@PathVariable Long orderId,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        orderChatService.markOthersMessagesRead(orderId, principal.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long orderId,
                                         @PathVariable Long messageId,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        ChatMessageDto dto = orderChatService.softDeleteOwnMessage(orderId, messageId, principal.getId());
        return ResponseEntity.ok(dto);
    }

    private static ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Не авторизован"));
    }
}
