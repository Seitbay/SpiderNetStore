package ru.SeitbayBulat.SpiderNetStore.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendChatMessageRequest {
    @NotBlank
    @Size(max = 8000)
    private String text;
}
