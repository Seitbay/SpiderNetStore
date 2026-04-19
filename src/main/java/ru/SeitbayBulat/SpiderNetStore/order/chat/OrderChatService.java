package ru.SeitbayBulat.SpiderNetStore.order.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.SeitbayBulat.SpiderNetStore.order.Order;
import ru.SeitbayBulat.SpiderNetStore.order.OrderRepository;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ChatMessageDto;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ChatMessagesPageDto;
import ru.SeitbayBulat.SpiderNetStore.order.dto.ChatUnreadSummaryDto;
import ru.SeitbayBulat.SpiderNetStore.user.User;
import ru.SeitbayBulat.SpiderNetStore.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderChatService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    @Value("${app.chat.delete-grace-minutes:15}")
    private int deleteGraceMinutes;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReadReceiptRepository chatMessageReadReceiptRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final OrderChatRealtimePublisher orderChatRealtimePublisher;

    @Transactional
    public ChatMessage sendMessage(Long orderId, Long senderId, String text) {
        Order order = orderRepository.findByIdAndParticipant(orderId, senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        ChatMessage message = new ChatMessage();
        message.setOrder(order);
        message.setSender(sender);
        message.setText(text.trim());
        ChatMessage saved = chatMessageRepository.save(message);
        orderChatRealtimePublisher.publishNewMessage(orderId, chatMessageMapper.toDtoJustPosted(saved));
        return saved;
    }

    @Transactional(readOnly = true)
    public ChatMessagesPageDto getMessagesPage(
            Long orderId,
            Long viewerId,
            Long beforeId,
            Long afterId,
            Integer limitParam
    ) {
        int limit = clampLimit(limitParam);
        Order order = orderRepository.findByIdAndParticipant(orderId, viewerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        Long counterpartyId = resolveCounterpartyId(order, viewerId);
        Pageable pageable = PageRequest.of(0, limit);

        List<ChatMessage> batch;
        if (afterId != null) {
            batch = chatMessageRepository.findByOrder_IdAndIdGreaterThanOrderByIdAsc(orderId, afterId, pageable);
        } else if (beforeId != null) {
            batch = new ArrayList<>(
                    chatMessageRepository.findByOrder_IdAndIdLessThanOrderByIdDesc(orderId, beforeId, pageable)
            );
            Collections.reverse(batch);
        } else {
            batch = new ArrayList<>(chatMessageRepository.findByOrder_IdOrderByIdDesc(orderId, pageable));
            Collections.reverse(batch);
        }

        if (batch.isEmpty()) {
            return ChatMessagesPageDto.builder()
                    .items(List.of())
                    .hasOlder(false)
                    .oldestMessageId(null)
                    .newestMessageId(null)
                    .build();
        }

        List<Long> ids = batch.stream().map(ChatMessage::getId).toList();
        Map<Long, LocalDateTime> readByViewer = receiptTimesByMessageId(ids, viewerId);
        Map<Long, LocalDateTime> readByCounterparty = receiptTimesByMessageId(ids, counterpartyId);

        List<ChatMessageDto> items = batch.stream()
                .map(m -> chatMessageMapper.toDto(m, viewerId, readByViewer, readByCounterparty))
                .toList();

        long oldestId = batch.get(0).getId();
        long newestId = batch.get(batch.size() - 1).getId();
        boolean hasOlder = chatMessageRepository.existsByOrder_IdAndIdLessThan(orderId, oldestId);

        return ChatMessagesPageDto.builder()
                .items(items)
                .hasOlder(hasOlder)
                .oldestMessageId(oldestId)
                .newestMessageId(newestId)
                .build();
    }

    @Transactional
    public void markOthersMessagesRead(Long orderId, Long readerId) {
        orderRepository.findByIdAndParticipant(orderId, readerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        List<ChatMessage> unread = chatMessageRepository.findIncomingWithoutReceipt(orderId, readerId);
        if (unread.isEmpty()) {
            return;
        }
        List<Long> affectedIds = unread.stream().map(ChatMessage::getId).toList();
        User reader = userRepository.findById(readerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        LocalDateTime now = LocalDateTime.now();
        List<ChatMessageReadReceipt> receipts = new ArrayList<>(unread.size());
        for (ChatMessage m : unread) {
            ChatMessageReadReceipt r = new ChatMessageReadReceipt();
            r.setMessage(m);
            r.setReader(reader);
            r.setReadAt(now);
            receipts.add(r);
        }
        chatMessageReadReceiptRepository.saveAll(receipts);
        orderChatRealtimePublisher.publishReadReceipt(orderId, readerId, affectedIds, now);
    }

    @Transactional(readOnly = true)
    public ChatUnreadSummaryDto unreadSummary(Long userId) {
        List<Object[]> rows = chatMessageRepository.countUnreadIncomingGroupedByOrder(userId);
        Map<Long, Long> byOrder = new LinkedHashMap<>();
        long total = 0;
        for (Object[] row : rows) {
            Long oid = (Long) row[0];
            long cnt = ((Number) row[1]).longValue();
            byOrder.put(oid, cnt);
            total += cnt;
        }
        return ChatUnreadSummaryDto.builder()
                .totalUnread(total)
                .byOrderId(byOrder)
                .build();
    }

    @Transactional
    public ChatMessageDto softDeleteOwnMessage(Long orderId, Long messageId, Long userId) {
        Order order = orderRepository.findByIdAndParticipant(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        ChatMessage message = chatMessageRepository.findByIdAndOrder_Id(messageId, orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Сообщение не найдено"));
        if (!message.getSender().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Можно удалять только свои сообщения");
        }
        if (message.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Сообщение уже удалено");
        }
        LocalDateTime sent = message.getSentAt();
        if (sent == null || sent.plusMinutes(deleteGraceMinutes).isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Удаление возможно только в течение " + deleteGraceMinutes + " минут после отправки");
        }
        message.setDeletedAt(LocalDateTime.now());
        message.setText("");
        chatMessageRepository.save(message);

        Long counterpartyId = resolveCounterpartyId(order, userId);
        List<Long> mid = List.of(message.getId());
        Map<Long, LocalDateTime> readByViewer = receiptTimesByMessageId(mid, userId);
        Map<Long, LocalDateTime> readByCounterparty = receiptTimesByMessageId(mid, counterpartyId);
        ChatMessageDto dto = chatMessageMapper.toDto(message, userId, readByViewer, readByCounterparty);
        orderChatRealtimePublisher.publishMessageDeleted(orderId, dto);
        return dto;
    }

    private static int clampLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private static Long resolveCounterpartyId(Order order, Long viewerId) {
        Long buyerId = order.getBuyer().getId();
        Long sellerId = order.getProduct().getSeller().getId();
        if (viewerId.equals(buyerId)) {
            return sellerId;
        }
        if (viewerId.equals(sellerId)) {
            return buyerId;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к заказу");
    }

    private Map<Long, LocalDateTime> receiptTimesByMessageId(List<Long> messageIds, Long readerId) {
        if (messageIds.isEmpty()) {
            return Map.of();
        }
        return chatMessageReadReceiptRepository.findByMessage_IdInAndReader_Id(messageIds, readerId).stream()
                .collect(Collectors.toMap(r -> r.getMessage().getId(), ChatMessageReadReceipt::getReadAt, (a, b) -> a));
    }
}
