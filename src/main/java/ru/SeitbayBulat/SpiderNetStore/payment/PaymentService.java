package ru.SeitbayBulat.SpiderNetStore.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.SeitbayBulat.SpiderNetStore.user.User;
import ru.SeitbayBulat.SpiderNetStore.user.UserRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal MAX_SINGLE_OPERATION = new BigDecimal("999999.99");

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        return nonNullBalance(user);
    }

    @Transactional
    public Transaction deposit(Long userId, BigDecimal amount) {
        validatePositiveAmount(amount);
        if (amount.compareTo(MAX_SINGLE_OPERATION) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Слишком большая сумма пополнения");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        BigDecimal balance = nonNullBalance(user);
        user.setBalance(balance.add(amount));
        userRepository.save(user);

        Transaction tx = new Transaction();
        tx.setOrder(null);
        tx.setFromUser(null);
        tx.setToUser(user);
        tx.setAmount(amount);
        tx.setType(TransactionType.DEPOSIT);
        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction payout(Long userId, BigDecimal amount) {
        validatePositiveAmount(amount);
        if (amount.compareTo(MAX_SINGLE_OPERATION) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Слишком большая сумма вывода");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        BigDecimal balance = nonNullBalance(user);
        if (balance.compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно средств для вывода");
        }

        user.setBalance(balance.subtract(amount));
        userRepository.save(user);

        Transaction tx = new Transaction();
        tx.setOrder(null);
        tx.setFromUser(user);
        tx.setToUser(null);
        tx.setAmount(amount);
        tx.setType(TransactionType.PAYOUT);
        return transactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> listMyTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserInvolved(userId, pageable);
    }

    private static BigDecimal nonNullBalance(User user) {
        BigDecimal b = user.getBalance();
        return b != null ? b : BigDecimal.ZERO;
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Сумма должна быть больше нуля");
        }
    }
}
