package ru.SeitbayBulat.SpiderNetStore.order;

import jakarta.validation.Valid;
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
import ru.SeitbayBulat.SpiderNetStore.order.dispute.DisputeMapper;
import ru.SeitbayBulat.SpiderNetStore.order.dispute.DisputeService;
import ru.SeitbayBulat.SpiderNetStore.order.dto.CreateOrderRequest;
import ru.SeitbayBulat.SpiderNetStore.order.dto.DisputeDto;
import ru.SeitbayBulat.SpiderNetStore.order.dto.OpenDisputeRequest;
import ru.SeitbayBulat.SpiderNetStore.order.dto.OrderDto;
import ru.SeitbayBulat.SpiderNetStore.order.dto.OrderListDto;
import ru.SeitbayBulat.SpiderNetStore.order.dto.OrderMapper;
import ru.SeitbayBulat.SpiderNetStore.user.security.UserPrincipal;

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
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        Order order = orderService.createOrder(principal.getId(), request.getProductId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<OrderDto> completeOrder(@PathVariable Long orderId,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        Order order = orderService.completeOrder(orderId, principal.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long orderId,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        Order order = orderService.cancelOrder(orderId, principal.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<OrderDto> refundOrder(@PathVariable Long orderId,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        Order order = orderService.refundOrder(orderId, principal.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @PostMapping("/{orderId}/dispute")
    public ResponseEntity<DisputeDto> openDispute(@PathVariable Long orderId,
                                                  @Valid @RequestBody OpenDisputeRequest req,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                disputeMapper.toDto(disputeService.openDispute(orderId, principal.getId(), req.getReason())));
    }

    @GetMapping("/my")
    public ResponseEntity<OrderListDto> myOrders(@AuthenticationPrincipal UserPrincipal principal,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        Page<Order> data = orderService.listBuyerOrders(principal.getId(), PageRequest.of(page, clampSize(size)));
        return ResponseEntity.ok(orderMapper.toListDto(data));
    }

    @GetMapping("/sales")
    public ResponseEntity<OrderListDto> salesOrders(@AuthenticationPrincipal UserPrincipal principal,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        Page<Order> data = orderService.listSellerOrders(principal.getId(), PageRequest.of(page, clampSize(size)));
        return ResponseEntity.ok(orderMapper.toListDto(data));
    }

    @GetMapping("/{orderId}/dispute")
    public ResponseEntity<DisputeDto> getDisputeForOrder(@PathVariable Long orderId,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                disputeMapper.toDto(disputeService.getDisputeForOrder(orderId, principal.getId())));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId,
                                             @AuthenticationPrincipal UserPrincipal principal) {
        Order order = orderService.getOrderForParticipant(orderId, principal.getId());
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    private static int clampSize(int size) {
        if (size < 1) {
            return 1;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
