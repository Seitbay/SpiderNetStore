package ru.SeitbayBulat.SpiderNetStore.order.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private Long id;
    private Long orderId;
    private Long senderId;
    private String senderUsername;
    private String text;
    /**
     * С точки зрения текущего пользователя: входящее прочитано им;
     * исходящее — прочитано собеседником.
     */
    private boolean read;
    /**
     * ISO-8601 время соответствующего факта прочтения ({@code read}), иначе {@code null}.
     */
    private String readAt;
    private String sentAt;
    /** Сообщение удалено отправителем */
    private boolean deleted;
}
