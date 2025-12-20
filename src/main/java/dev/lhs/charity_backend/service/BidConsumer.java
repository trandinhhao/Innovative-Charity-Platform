package dev.lhs.charity_backend.service;

/**
 * Consumer xử lý bid request từ queue
 * Đọc message → validate với Redis → xử lý với DB (pessimistic lock) → update Redis
 */
public interface BidConsumer {
    
    /**
     * Xử lý bid request từ queue
     * Method này sẽ được gọi bởi RabbitMQ listener
     */
    void processBidRequest(String message);
}

