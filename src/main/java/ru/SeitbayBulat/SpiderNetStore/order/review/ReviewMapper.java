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
        dto.setBuyerUsername(r.getBuyer().getUsername());
        dto.setCreatedAt(r.getCreatedAt().toString()); // позже можно форматнуть

        return dto;
    }
}
