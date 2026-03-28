package ru.SeitbayBulat.SpiderNetStore.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderListDto {
    private List<OrderDto> items;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
}
