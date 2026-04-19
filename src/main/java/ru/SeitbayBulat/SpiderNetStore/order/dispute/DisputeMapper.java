package ru.SeitbayBulat.SpiderNetStore.order.dispute;

import org.springframework.stereotype.Component;
import ru.SeitbayBulat.SpiderNetStore.order.dto.DisputeDto;

@Component
public class DisputeMapper {

    public DisputeDto toDto(Dispute d) {
        DisputeDto dto = new DisputeDto();
        dto.setId(d.getId());
        if (d.getOrder() != null) {
            dto.setOrderId(d.getOrder().getId());
        }
        dto.setStatus(d.getStatus().name());
        dto.setReason(d.getReason());
        dto.setSellerResponse(d.getSellerResponse());
        dto.setResolution(d.getResolution());
        if (d.getInitiator() != null) {
            dto.setInitiatorId(d.getInitiator().getId());
            dto.setInitiatorUsername(d.getInitiator().getUsername());
        }
        if (d.getResolvedBy() != null) {
            dto.setResolvedById(d.getResolvedBy().getId());
            dto.setResolvedByUsername(d.getResolvedBy().getUsername());
        }
        if (d.getCreatedAt() != null) {
            dto.setCreatedAt(d.getCreatedAt().toString());
        }
        if (d.getResolvedAt() != null) {
            dto.setResolvedAt(d.getResolvedAt().toString());
        }
        return dto;
    }
}
