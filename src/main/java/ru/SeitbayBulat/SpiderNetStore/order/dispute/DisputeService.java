package ru.SeitbayBulat.SpiderNetStore.order.dispute;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.SeitbayBulat.SpiderNetStore.order.Order;
import ru.SeitbayBulat.SpiderNetStore.order.OrderRepository;
import ru.SeitbayBulat.SpiderNetStore.order.OrderStatus;
import ru.SeitbayBulat.SpiderNetStore.user.User;
import ru.SeitbayBulat.SpiderNetStore.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public Dispute openDispute(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findByIdAndParticipant(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        requireOrderStatus(order, OrderStatus.COMPLETED);
        if (disputeRepository.findByOrder_Id(orderId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Спор по этому заказу уже открыт");
        }

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        Dispute dispute = new Dispute();
        dispute.setOrder(order);
        dispute.setInitiator(initiator);
        dispute.setReason(reason);
        dispute.setStatus(DisputeStatus.OPEN);
        order.setDispute(dispute);
        order.setStatus(OrderStatus.DISPUTED);

        disputeRepository.save(dispute);
        orderRepository.save(order);
        return dispute;
    }

    @Transactional(readOnly = true)
    public Dispute getDisputeForOrder(Long orderId, Long userId) {
        orderRepository.findByIdAndParticipant(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        return disputeRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Спор не найден"));
    }

    @Transactional(readOnly = true)
    public Dispute getDisputeForParticipant(Long disputeId, Long userId) {
        return disputeRepository.findByIdAndParticipant(disputeId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Спор не найден"));
    }

    @Transactional
    public Dispute respondAsSeller(Long disputeId, Long sellerId, String response) {
        Dispute dispute = disputeRepository.findByIdAndOrder_Product_Seller_Id(disputeId, sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Спор не найден"));
        requireDisputeStatus(dispute, DisputeStatus.OPEN);
        dispute.setSellerResponse(response);
        dispute.setStatus(DisputeStatus.SELLER_RESPONDED);
        return disputeRepository.save(dispute);
    }

    @Transactional
    public Dispute escalate(Long disputeId, Long userId) {
        Dispute dispute = disputeRepository.findByIdAndParticipant(disputeId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Спор не найден"));
        requireDisputeStatus(dispute, DisputeStatus.OPEN, DisputeStatus.SELLER_RESPONDED);
        dispute.setStatus(DisputeStatus.ESCALATED);
        return disputeRepository.save(dispute);
    }

    @Transactional
    public Dispute resolveByModerator(Long disputeId, Long moderatorId, DisputeStatus outcome, String resolutionText) {
        if (outcome != DisputeStatus.RESOLVED_BUYER && outcome != DisputeStatus.RESOLVED_SELLER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Итог должен быть RESOLVED_BUYER или RESOLVED_SELLER");
        }
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Спор не найден"));
        if (isTerminal(dispute.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Спор уже закрыт");
        }
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Модератор не найден"));

        dispute.setStatus(outcome);
        dispute.setResolution(resolutionText);
        dispute.setResolvedBy(moderator);
        dispute.setResolvedAt(LocalDateTime.now());
        return disputeRepository.save(dispute);
    }

    private static boolean isTerminal(DisputeStatus s) {
        return s == DisputeStatus.RESOLVED_BUYER || s == DisputeStatus.RESOLVED_SELLER;
    }

    private static void requireOrderStatus(Order order, OrderStatus... allowed) {
        OrderStatus current = order.getStatus();
        if (Arrays.stream(allowed).anyMatch(x -> x == current)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Ожидался статус заказа из " + Arrays.toString(allowed) + ", сейчас: " + current);
    }

    private static void requireDisputeStatus(Dispute dispute, DisputeStatus... allowed) {
        DisputeStatus current = dispute.getStatus();
        if (Arrays.stream(allowed).anyMatch(x -> x == current)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Ожидался статус спора из " + Arrays.toString(allowed) + ", сейчас: " + current);
    }
}
