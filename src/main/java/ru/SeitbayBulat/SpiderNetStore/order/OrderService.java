package ru.SeitbayBulat.SpiderNetStore.order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
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
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Order createOrder(Long buyerId, Long productId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Покупатель не найден"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден"));

        if (product.getSeller().getId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя купить свой товар");
        }

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Товар недоступен для покупки");
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
    public Order completeOrder(Long orderId, Long buyerId) {
        Order order = orderRepository.findByIdAndBuyer_Id(orderId, buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        requireStatus(order, OrderStatus.PENDING);

        Long sellerId = order.getProduct().getSeller().getId();
        BigDecimal amount = order.getAmount();

        TransferParties parties = transferBetweenUsers(
                buyerId,
                sellerId,
                amount,
                "Недостаточно средств на балансе");

        recordTransaction(order, parties.from(), parties.to(), amount, TransactionType.PURCHASE);

        stockService.confirmSale(order.getStockItem());

        order.setStatus(OrderStatus.COMPLETED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long buyerId) {
        Order order = orderRepository.findByIdAndBuyer_Id(orderId, buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        requireStatus(order, OrderStatus.PENDING);

        stockService.releaseStockItem(order.getStockItem());
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order refundOrder(Long orderId, Long sellerId) {
        Order order = orderRepository.findByIdAndProduct_Seller_Id(orderId, sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        requireStatus(order, OrderStatus.COMPLETED, OrderStatus.DISPUTED);

        Long buyerId = order.getBuyer().getId();
        BigDecimal amount = order.getAmount();

        TransferParties parties = transferBetweenUsers(
                sellerId,
                buyerId,
                amount,
                "На балансе продавца недостаточно средств для возврата");

        recordTransaction(order, parties.from(), parties.to(), amount, TransactionType.REFUND);

        order.setStatus(OrderStatus.REFUNDED);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderForParticipant(Long orderId, Long userId) {
        return orderRepository.findByIdAndParticipant(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
    }

    @Transactional(readOnly = true)
    public Page<Order> listBuyerOrders(Long buyerId, Pageable pageable) {
        return orderRepository.findByBuyer_IdOrderByCreatedAtDesc(buyerId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> listSellerOrders(Long sellerId, Pageable pageable) {
        return orderRepository.findByProduct_Seller_IdOrderByCreatedAtDesc(sellerId, pageable);
    }

    private void requireStatus(Order order, OrderStatus... allowed) {
        OrderStatus current = order.getStatus();
        if (Arrays.stream(allowed).anyMatch(s -> s == current)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Операция недоступна для статуса " + current + ", допустимо: " + Arrays.toString(allowed));
    }

    /**
     * Перевод между пользователями с блокировкой строк (по возрастанию id — меньше риск взаимоблокировки)
     * и проверкой баланса отправителя уже под блокировкой.
     */
    private TransferParties transferBetweenUsers(Long fromUserId, Long toUserId, BigDecimal amount,
                                                 String insufficientFundsMessage) {
        long minId = Math.min(fromUserId, toUserId);
        long maxId = Math.max(fromUserId, toUserId);

        User lockedMin = userRepository.findByIdForUpdate(minId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        User lockedMax = userRepository.findByIdForUpdate(maxId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        User from = fromUserId.equals(lockedMin.getId()) ? lockedMin : lockedMax;
        User to = toUserId.equals(lockedMin.getId()) ? lockedMin : lockedMax;

        BigDecimal fromBalance = nonNullBalance(from.getBalance());
        if (fromBalance.compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, insufficientFundsMessage);
        }

        from.setBalance(fromBalance.subtract(amount));
        to.setBalance(nonNullBalance(to.getBalance()).add(amount));
        userRepository.save(from);
        userRepository.save(to);

        return new TransferParties(from, to);
    }

    private void recordTransaction(Order order, User from, User to, BigDecimal amount, TransactionType type) {
        Transaction tx = new Transaction();
        tx.setOrder(order);
        tx.setFromUser(from);
        tx.setToUser(to);
        tx.setAmount(amount);
        tx.setType(type);
        transactionRepository.save(tx);
    }

    private record TransferParties(User from, User to) {
    }

    private static BigDecimal nonNullBalance(BigDecimal balance) {
        return balance != null ? balance : BigDecimal.ZERO;
    }
}
