package ru.SeitbayBulat.SpiderNetStore.product.stock;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.SeitbayBulat.SpiderNetStore.product.Product;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stock_items")
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * JSON-данные (строка линии, объект, метаданные архива). Колонка jsonb.
     */
    @Column(nullable = false, columnDefinition = "jsonb")
    private String data;

    @Enumerated(EnumType.STRING)
    @Column(name = "payload_type", nullable = false, length = 30)
    private StockPayloadType payloadType = StockPayloadType.JSON_OBJECT;

    /** Для {@link StockPayloadType#ARCHIVE_FILE} — содержимое файла. */
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "binary_payload")
    private byte[] binaryPayload;

    @Column(name = "original_filename")
    private String originalFilename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockItemStatus status = StockItemStatus.AVAILABLE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
