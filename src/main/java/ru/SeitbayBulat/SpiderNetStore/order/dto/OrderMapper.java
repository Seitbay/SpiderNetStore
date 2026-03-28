package ru.SeitbayBulat.SpiderNetStore.order.dto;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.SeitbayBulat.SpiderNetStore.order.Order;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus().name());
        dto.setAmount(order.getAmount());
        dto.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);
        dto.setBuyerId(order.getBuyer() != null ? order.getBuyer().getId() : null);
        dto.setBuyerUsername(order.getBuyer() != null ? order.getBuyer().getUsername() : null);
        dto.setProductId(order.getProduct() != null ? order.getProduct().getId() : null);
        dto.setProductTitle(order.getProduct() != null ? order.getProduct().getTitle() : null);
        if (order.getProduct() != null && order.getProduct().getSeller() != null) {
            dto.setSellerId(order.getProduct().getSeller().getId());
            dto.setSellerUsername(order.getProduct().getSeller().getUsername());
        }
        dto.setStockItemId(order.getStockItem() != null ? order.getStockItem().getId() : null);
        return dto;
    }

    public OrderListDto toListDto(Page<Order> page) {
        OrderListDto dto = new OrderListDto();
        dto.setItems(page.getContent().stream().map(this::toDto).toList());
        dto.setPage(page.getNumber());
        dto.setSize(page.getSize());
        dto.setTotalPages(page.getTotalPages());
        dto.setTotalElements(page.getTotalElements());
        return dto;
    }
}
