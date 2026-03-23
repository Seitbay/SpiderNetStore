package ru.SeitbayBulat.SpiderNetStore.order.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ReviewRequest;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Проверка: можно ли оставить отзыв
    @GetMapping("/products/{productId}/review-status")
    public ResponseEntity<?> canLeaveReview(@PathVariable Long productId,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Map.of("canLeaveReview", false));
        }

        boolean can = reviewService.canLeaveReview(principal.getId(), productId);
        return ResponseEntity.ok(Map.of("canLeaveReview", can));
    }

    // Создание отзыва
    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest req,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }

        reviewService.createReview(principal.getId(), req);
        return ResponseEntity.ok(Map.of("success", true));
    }
}