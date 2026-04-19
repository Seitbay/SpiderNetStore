package ru.SeitbayBulat.SpiderNetStore.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.SeitbayBulat.SpiderNetStore.order.dispute.DisputeStatus;

@Data
public class ResolveDisputeRequest {
    /** Только {@link DisputeStatus#RESOLVED_BUYER} или {@link DisputeStatus#RESOLVED_SELLER}. */
    @NotNull
    private DisputeStatus outcome;

    @NotBlank
    private String resolution;
}
