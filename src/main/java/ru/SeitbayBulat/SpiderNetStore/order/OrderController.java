package ru.SeitbayBulat.SpiderNetStore.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.SeitbayBulat.SpiderNetStore.order.dispute.Dispute;
import ru.SeitbayBulat.SpiderNetStore.order.dispute.DisputeMapper;
import ru.SeitbayBulat.SpiderNetStore.order.dispute.DisputeService;
import ru.SeitbayBulat.SpiderNetStore.order.dto.CreateOrderRequest;
import ru.SeitbayBulat.SpiderNetStore.order.dto.OpenDisputeRequest;
import ru.SeitbayBulat.SpiderNetStore.order.dto.OrderMapper;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final int MAX_PAGE_SIZE = 100;

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final DisputeService disputeService;
    private final DisputeMapper disputeMapper;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        Order order = orderService.createOrder(principal.getId(), request.getProductId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long orderId,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        Order order = orderService.completeOrder(orderId, principal.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        Order order = orderService.cancelOrder(orderId, principal.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<?> refundOrder(@PathVariable Long orderId,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        Order order = orderService.refundOrder(orderId, principal.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @PostMapping("/{orderId}/dispute")
    public ResponseEntity<?> openDispute(@PathVariable Long orderId,
                                         @Valid @RequestBody OpenDisputeRequest req,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        Dispute dispute = disputeService.openDispute(orderId, principal.getId(), req.getReason());
        return ResponseEntity.ok(disputeMapper.toDto(dispute));
    }

    @GetMapping("/my")
    public ResponseEntity<?> myOrders(@AuthenticationPrincipal UserPrincipal principal,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        if (principal == null) {
            return unauthorized();
        }
        Page<Order> data = orderService.listBuyerOrders(principal.getId(), PageRequest.of(page, clampSize(size)));
        return ResponseEntity.ok(orderMapper.toListDto(data));
    }

    @GetMapping("/sales")
    public ResponseEntity<?> salesOrders(@AuthenticationPrincipal UserPrincipal principal,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        if (principal == null) {
            return unauthorized();
        }
        Page<Order> data = orderService.listSellerOrders(principal.getId(), PageRequest.of(page, clampSize(size)));
        return ResponseEntity.ok(orderMapper.toListDto(data));
    }

    @GetMapping("/{orderId}/dispute")
    public ResponseEntity<?> getDisputeForOrder(@PathVariable Long orderId,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        Dispute dispute = disputeService.getDisputeForOrder(orderId, principal.getId());
        return ResponseEntity.ok(disputeMapper.toDto(dispute));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return unauthorized();
        }
        Order order = orderService.getOrderForParticipant(orderId, principal.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
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
