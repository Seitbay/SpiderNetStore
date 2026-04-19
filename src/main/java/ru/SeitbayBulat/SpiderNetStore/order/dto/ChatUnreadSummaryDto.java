package ru.SeitbayBulat.SpiderNetStore.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Сводка непрочитанных входящих сообщений по заказам (фаза 3).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatUnreadSummaryDto {
    private long totalUnread;
    /** id заказа → количество непрочитанных входящих */
    private Map<Long, Long> byOrderId;
}
