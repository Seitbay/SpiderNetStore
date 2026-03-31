package ru.SeitbayBulat.SpiderNetStore.payment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.SeitbayBulat.SpiderNetStore.payment.dto.BalanceDto;
import ru.SeitbayBulat.SpiderNetStore.payment.dto.DepositRequest;
import ru.SeitbayBulat.SpiderNetStore.payment.dto.PayoutRequest;
import ru.SeitbayBulat.SpiderNetStore.payment.dto.TransactionMapper;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PaymentService paymentService;
    private final TransactionMapper transactionMapper;

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        return ResponseEntity.ok(new BalanceDto(paymentService.getBalance(principal.getId())));
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositRequest request,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        Transaction tx = paymentService.deposit(principal.getId(), request.getAmount());
        return ResponseEntity.ok(transactionMapper.toDto(tx));
    }

    @PostMapping("/payout")
    public ResponseEntity<?> payout(@Valid @RequestBody PayoutRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        Transaction tx = paymentService.payout(principal.getId(), request.getAmount());
        return ResponseEntity.ok(transactionMapper.toDto(tx));
    }

    @GetMapping("/my")
    public ResponseEntity<?> myTransactions(@AuthenticationPrincipal UserPrincipal principal,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        if (principal == null) {
            return unauthorized();
        }
        Page<Transaction> data = paymentService.listMyTransactions(principal.getId(),
                PageRequest.of(page, clampSize(size)));
        return ResponseEntity.ok(transactionMapper.toListDto(data));
    }

    private static ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Не авторизован"));
    }

    private static int clampSize(int size) {
        if (size < 1) {
            return 1;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
