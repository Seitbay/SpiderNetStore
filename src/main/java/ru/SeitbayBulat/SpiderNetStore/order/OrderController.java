package ru.SeitbayBulat.SpiderNetStore.order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.SeitbayBulat.SpiderNetStore.order.dto.CreateOrderRequest;
import ru.SeitbayBulat.SpiderNetStore.order.dto.OrderMapper;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }
        if (request.getProductId() == null) {
            return ResponseEntity.status(400).body(Map.of("error", "ProductId обязателен"));
        }
        Order order = orderService.createOrder(principal.getId(), request.getProductId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long orderId,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }
        Order order = orderService.completeOrder(orderId, principal.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }
        Order order = orderService.cancelOrder(orderId, principal.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<?> refundOrder(@PathVariable Long orderId,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }
        Order order = orderService.refundOrder(orderId, principal.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }
        Order order = orderService.getOrderForParticipant(orderId, principal.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }
    

    @GetMapping("/my")
    public ResponseEntity<?> myOrders(@AuthenticationPrincipal UserPrincipal principal,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }
        Page<Order> data = orderService.listBuyerOrders(principal.getId(), PageRequest.of(page, size));
        return ResponseEntity.ok(orderMapper.toListDto(data));
    }

    @GetMapping("/sales")
    public ResponseEntity<?> salesOrders(@AuthenticationPrincipal UserPrincipal principal,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }
        Page<Order> data = orderService.listSellerOrders(principal.getId(), PageRequest.of(page, size));
        return ResponseEntity.ok(orderMapper.toListDto(data));
    }
}

