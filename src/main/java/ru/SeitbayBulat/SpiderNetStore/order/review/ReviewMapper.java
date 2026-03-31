package ru.SeitbayBulat.SpiderNetStore.order.review;

import org.springframework.stereotype.Component;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ReviewDto;

@Component
public class ReviewMapper {
    public ReviewDto toDto(Review r) {
        ReviewDto dto = new ReviewDto();
        dto.setId(r.getId());
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        if (r.getBuyer() != null) {
            dto.setBuyerId(r.getBuyer().getId());
            dto.setBuyerUsername(r.getBuyer().getUsername());
        }
        if (r.getCreatedAt() != null) {
            dto.setCreatedAt(r.getCreatedAt().toString());
        }
        return dto;
    }
}
