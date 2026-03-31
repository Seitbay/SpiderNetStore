package ru.SeitbayBulat.SpiderNetStore.order.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
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

    private static final List<OrderStatus> STATUSES_ELIGIBLE_FOR_REVIEW = List.of(
            OrderStatus.COMPLETED,
            OrderStatus.DISPUTED
    );

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public boolean canLeaveReview(Long userId, Long productId) {
        User buyer = userRepository.findById(userId).orElse(null);
        if (buyer == null) {
            return false;
        }

        return orderRepository
                .findTopByBuyerAndProduct_IdAndStatusInAndReviewIsNullOrderByIdDesc(
                        buyer, productId, STATUSES_ELIGIBLE_FOR_REVIEW)
                .isPresent();
    }

    @Transactional
    public void createReview(Long userId, ReviewRequest req) {
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден"));

        Order order = orderRepository
                .findTopByBuyerAndProduct_IdAndStatusInAndReviewIsNullOrderByIdDesc(
                        buyer, req.getProductId(), STATUSES_ELIGIBLE_FOR_REVIEW)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Нет подходящего заказа или отзыв уже оставлен"));

        Review review = new Review();
        review.setOrder(order);
        review.setBuyer(buyer);
        review.setProduct(product);
        review.setRating(req.getRating());
        review.setComment(req.getComment());

        order.setReview(review);
        reviewRepository.save(review);

        double avg = reviewRepository.findByProduct_Id(product.getId())
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
