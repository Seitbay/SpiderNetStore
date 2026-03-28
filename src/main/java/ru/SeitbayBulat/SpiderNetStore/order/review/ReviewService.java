package ru.SeitbayBulat.SpiderNetStore.order.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.SeitbayBulat.SpiderNetStore.order.Order;
import ru.SeitbayBulat.SpiderNetStore.order.OrderRepository;
import ru.SeitbayBulat.SpiderNetStore.order.OrderStatus;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ReviewDto;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ReviewRequest;
import ru.SeitbayBulat.SpiderNetStore.product.Product;
import ru.SeitbayBulat.SpiderNetStore.product.ProductRepository;
import ru.SeitbayBulat.SpiderNetStore.user.User;
import ru.SeitbayBulat.SpiderNetStore.user.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    // todo сильная связность с кучей других репозиториев которые не находятся в одном логическом домене -> фиксануть!
    // на самом деле хз вроде пойдет, ох и будут проблемы при распиле этого говна на куски ('><')
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public boolean canLeaveReview(Long userId, Long productId) {
        User buyer = userRepository.findById(userId).orElse(null);
        if (buyer == null) return false;

        return orderRepository
                .findTopByBuyerAndProduct_IdAndStatusAndReviewIsNullOrderByIdDesc(
                        buyer, productId, OrderStatus.COMPLETED)
                .isPresent();
    }

    @Transactional
    public void createReview(Long userId, ReviewRequest req) {
        User buyer = userRepository.findById(userId).orElseThrow();
        Product product = productRepository.findById(req.getProductId()).orElseThrow();

        Order order = orderRepository
                .findTopByBuyerAndProduct_IdAndStatusAndReviewIsNullOrderByIdDesc(
                        buyer, req.getProductId(), OrderStatus.COMPLETED)
                .orElseThrow(() -> new RuntimeException("Заказ не найден или отзыв уже оставлен"));

        Review review = new Review();
        review.setOrder(order);
        review.setBuyer(buyer);
        review.setProduct(product);
        review.setRating(req.getRating());
        review.setComment(req.getComment());

        order.setReview(review);
        reviewRepository.save(review);

        // Пересчёт среднего рейтинга продукта
        double avg = reviewRepository.findByProductId(product.getId())
                .stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);

        product.setRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        productRepository.save(product);
    }
    
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductIdWithBuyer(productId)
                .stream()
                .map(reviewMapper::toDto)
                .toList();
    }
}