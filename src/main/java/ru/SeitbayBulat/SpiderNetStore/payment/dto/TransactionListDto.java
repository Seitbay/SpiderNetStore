package ru.SeitbayBulat.SpiderNetStore.payment.dto;

import lombok.Data;

import java.util.List;

@Data
public class TransactionListDto {
    private List<TransactionDto> items;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
}
