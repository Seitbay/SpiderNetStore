package ru.SeitbayBulat.SpiderNetStore.payment.dto;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.SeitbayBulat.SpiderNetStore.payment.Transaction;

@Component
public class TransactionMapper {

    public TransactionDto toDto(Transaction t) {
        TransactionDto dto = new TransactionDto();
        dto.setId(t.getId());
        dto.setType(t.getType().name());
        dto.setAmount(t.getAmount());
        dto.setCreatedAt(t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
        dto.setOrderId(t.getOrder() != null ? t.getOrder().getId() : null);
        if (t.getFromUser() != null) {
            dto.setFromUserId(t.getFromUser().getId());
            dto.setFromUsername(t.getFromUser().getUsername());
        }
        if (t.getToUser() != null) {
            dto.setToUserId(t.getToUser().getId());
            dto.setToUsername(t.getToUser().getUsername());
        }
        return dto;
    }

    public TransactionListDto toListDto(Page<Transaction> page) {
        TransactionListDto dto = new TransactionListDto();
        dto.setItems(page.getContent().stream().map(this::toDto).toList());
        dto.setPage(page.getNumber());
        dto.setSize(page.getSize());
        dto.setTotalPages(page.getTotalPages());
        dto.setTotalElements(page.getTotalElements());
        return dto;
    }
}
