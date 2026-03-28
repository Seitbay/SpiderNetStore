package ru.SeitbayBulat.SpiderNetStore.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.SeitbayBulat.SpiderNetStore.payment.dto.DepositRequest;
import ru.SeitbayBulat.SpiderNetStore.payment.dto.PayoutRequest;
import ru.SeitbayBulat.SpiderNetStore.payment.dto.TransactionMapper;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final TransactionMapper transactionMapper;

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositRequest request,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }
        Transaction tx = paymentService.deposit(principal.getId(), request.amount());
        return ResponseEntity.ok(transactionMapper.toDto(tx));
    }

    @PostMapping("/payout")
    public ResponseEntity<?> payout(@RequestBody PayoutRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }
        Transaction tx = paymentService.payout(principal.getId(), request.amount());
        return ResponseEntity.ok(transactionMapper.toDto(tx));
    }

    @GetMapping("/my")
    public ResponseEntity<?> myTransactions(@AuthenticationPrincipal UserPrincipal principal,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }
        Page<Transaction> data = paymentService.listMyTransactions(principal.getId(), PageRequest.of(page, size));
        return ResponseEntity.ok(transactionMapper.toListDto(data));
    }
}
