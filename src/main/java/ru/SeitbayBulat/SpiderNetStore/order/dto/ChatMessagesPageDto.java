package ru.SeitbayBulat.SpiderNetStore.order.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Страница истории чата по заказу (курсоры по id сообщения).
 */
@Data
@Builder
public class ChatMessagesPageDto {
    private List<ChatMessageDto> items;
    /** Есть ли сообщения старше {@link #oldestMessageId} */
    private boolean hasOlder;
    /** Минимальный id в ответе — для следующего запроса {@code beforeId} */
    private Long oldestMessageId;
    /** Максимальный id в ответе — для поллинга {@code afterId} */
    private Long newestMessageId;
}
