package ru.SeitbayBulat.SpiderNetStore.order;

public enum OrderStatus {
    PENDING,    // создан, предмет зарезервирован, ждёт завершения или отмены
    COMPLETED,  // оплата прошла, сделка закрыта
    CANCELLED,  // отмена покупателем до завершения
    DISPUTED,   // спор по завершённому заказу
    REFUNDED    // возврат денег покупателю после COMPLETED
}
