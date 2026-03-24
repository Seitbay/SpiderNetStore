package ru.SeitbayBulat.SpiderNetStore.order.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ReviewDto;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ReviewRequest;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Создание отзыва
    @PostMapping("/review")
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest req,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }

        reviewService.createReview(principal.getId(), req);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Проверка: можно ли оставить отзыв
    @GetMapping("/product/{productId}/can-leave")
    public ResponseEntity<?> canLeaveReview(@PathVariable Long productId,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Map.of("canLeaveReview", false));
        }

        boolean can = reviewService.canLeaveReview(principal.getId(), productId);
        return ResponseEntity.ok(Map.of("canLeaveReview", can));
    }
    // получаем отзыв для конкретного продукта по id
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProductId(productId));
    }
}