package ru.SeitbayBulat.SpiderNetStore.order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.SeitbayBulat.SpiderNetStore.payment.Transaction;
import ru.SeitbayBulat.SpiderNetStore.payment.TransactionRepository;
import ru.SeitbayBulat.SpiderNetStore.payment.TransactionType;
import ru.SeitbayBulat.SpiderNetStore.product.Product;
import ru.SeitbayBulat.SpiderNetStore.product.ProductRepository;
import ru.SeitbayBulat.SpiderNetStore.product.ProductStatus;
import ru.SeitbayBulat.SpiderNetStore.product.stock.StockItem;
import ru.SeitbayBulat.SpiderNetStore.product.stock.StockService;
import ru.SeitbayBulat.SpiderNetStore.user.User;
import ru.SeitbayBulat.SpiderNetStore.user.UserRepository;

import java.math.BigDecimal;

public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Order createOrder(Long buyerId, Long productId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Покупатель не найден"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new RuntimeException("Товар недоступен для покупки");
        }
        StockItem stockItem = stockService.reserveStockItem(productId);

        Order order = new Order();
        order.setBuyer(buyer);
        order.setProduct(product);
        order.setStockItem(stockItem);
        order.setAmount(product.getPrice());
        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long buyerId) {}

    @Transactional
    public Order refundOrder(Long orderId, Long actorId) {}

    @Transactional(readOnly = true)
    public Order getOrderForParticipant(Long orderId, Long userId) {}

    @Transactional(readOnly = true)
    public Page<Order> listSellerOrders(Long buyerId, Pageable pageable) {}

    private void lockUsersInStableOrder(Long userId1, Long userId2){}
}
