package ru.SeitbayBulat.SpiderNetStore.order.chat;

import org.springframework.stereotype.Component;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ChatMessageDto;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class ChatMessageMapper {

    /**
     * Сообщение только что отправлено текущим пользователем: квитанций ещё нет.
     */
    public ChatMessageDto toDtoJustPosted(ChatMessage m) {
        ChatMessageDto dto = baseDto(m);
        dto.setRead(false);
        dto.setReadAt(null);
        dto.setDeleted(false);
        return dto;
    }

    /**
     * @param readAtWhenViewerRead         messageId → когда {@code viewerId} прочитал входящее
     * @param readAtWhenCounterpartyRead   messageId → когда собеседник прочитал исходящее от viewer
     */
    public ChatMessageDto toDto(
            ChatMessage m,
            Long viewerId,
            Map<Long, LocalDateTime> readAtWhenViewerRead,
            Map<Long, LocalDateTime> readAtWhenCounterpartyRead
    ) {
        ChatMessageDto dto = baseDto(m);
        Long sid = m.getSender().getId();
        if (sid.equals(viewerId)) {
            LocalDateTime t = readAtWhenCounterpartyRead.get(m.getId());
            dto.setRead(t != null);
            dto.setReadAt(t != null ? t.toString() : null);
        } else {
            LocalDateTime t = readAtWhenViewerRead.get(m.getId());
            dto.setRead(t != null);
            dto.setReadAt(t != null ? t.toString() : null);
        }
        return dto;
    }

    private static ChatMessageDto baseDto(ChatMessage m) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(m.getId());
        if (m.getOrder() != null) {
            dto.setOrderId(m.getOrder().getId());
        }
        if (m.getSender() != null) {
            dto.setSenderId(m.getSender().getId());
            dto.setSenderUsername(m.getSender().getUsername());
        }
        if (m.getDeletedAt() != null) {
            dto.setDeleted(true);
            dto.setText("Сообщение удалено");
        } else {
            dto.setDeleted(false);
            dto.setText(m.getText());
        }
        if (m.getSentAt() != null) {
            dto.setSentAt(m.getSentAt().toString());
        }
        return dto;
    }
}
