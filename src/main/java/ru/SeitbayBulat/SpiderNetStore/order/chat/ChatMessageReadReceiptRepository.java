package ru.SeitbayBulat.SpiderNetStore.order.chat;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ChatMessageReadReceiptRepository extends JpaRepository<ChatMessageReadReceipt, Long> {

    boolean existsByMessage_IdAndReader_Id(Long messageId, Long readerId);

    @EntityGraph(attributePaths = "message")
    List<ChatMessageReadReceipt> findByMessage_IdInAndReader_Id(Collection<Long> messageIds, Long readerId);
}
