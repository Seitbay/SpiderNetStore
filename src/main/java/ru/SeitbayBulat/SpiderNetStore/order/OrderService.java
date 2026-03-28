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

    /**
     * Покупатель подтверждает заказ: списание с баланса, зачисление продавцу, единица склада — SOLD.
     */
    @Transactional
    public Order completeOrder(Long orderId, Long buyerId) {
        Order order = orderRepository.findByIdAndBuyer_Id(orderId, buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Заказ нельзя завершить в текущем статусе");
        }

        User buyer = order.getBuyer();
        User seller = order.getProduct().getSeller();
        BigDecimal amount = order.getAmount();

        if (buyer.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно средств на балансе");
        }

        transferMoney(buyer, seller, amount);

        Transaction tx = new Transaction();
        tx.setOrder(order);
        tx.setFromUser(buyer);
        tx.setToUser(seller);
        tx.setAmount(amount);
        tx.setType(TransactionType.PURCHASE);
        transactionRepository.save(tx);

        stockService.confirmSale(order.getStockItem());

        order.setStatus(OrderStatus.COMPLETED);
        return orderRepository.save(order);
    }

    /**
     * Отмена ожидающего заказа покупателем: возврат позиции на склад.
     */
    @Transactional
    public Order cancelOrder(Long orderId, Long buyerId) {
        Order order = orderRepository.findByIdAndBuyer_Id(orderId, buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Отмена возможна только для ожидающего заказа");
        }

        stockService.releaseStockItem(order.getStockItem());
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    /**
     * Возврат средств покупателю после завершённой сделки (инициирует продавец).
     */
    @Transactional
    public Order refundOrder(Long orderId, Long sellerId) {
        Order order = orderRepository.findByIdAndProduct_Seller_Id(orderId, sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Возврат возможен только для завершённого заказа");
        }

        User buyer = order.getBuyer();
        User seller = order.getProduct().getSeller();
        BigDecimal amount = order.getAmount();

        if (seller.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "На балансе продавца недостаточно средств для возврата");
        }

        transferMoney(seller, buyer, amount);

        Transaction tx = new Transaction();
        tx.setOrder(order);
        tx.setFromUser(seller);
        tx.setToUser(buyer);
        tx.setAmount(amount);
        tx.setType(TransactionType.REFUND);
        transactionRepository.save(tx);

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

    /**
     * Списание с {@code from} и зачисление на {@code to}; сохранение пользователей в порядке id для согласованности блокировок.
     */
    private void transferMoney(User from, User to, BigDecimal amount) {
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        User first = from.getId() < to.getId() ? from : to;
        User second = from.getId() < to.getId() ? to : from;
        userRepository.save(first);
        userRepository.save(second);
    }
}
