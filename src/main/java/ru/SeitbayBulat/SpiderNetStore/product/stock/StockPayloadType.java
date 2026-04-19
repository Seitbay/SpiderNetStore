package ru.SeitbayBulat.SpiderNetStore.product.stock;

public enum StockPayloadType {
    /** Одна строка текстового файла. */
    TEXT_LINE,
    /** Один JSON-объект. */
    JSON_OBJECT,
    /** Один файл-архив (бинарно в БД). */
    ARCHIVE_FILE
}
