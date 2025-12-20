package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.response.BidResult;

import java.math.BigDecimal;

/**
 * Service xử lý đặt giá với Pessimistic Locking
 * Được gọi từ BidWorker (consumer từ queue)
 */
public interface BidService {
    
    /**
     * Xử lý một lượt đặt giá với Pessimistic Lock
     * Đảm bảo không có race condition
     * 
     * @param auctionId ID phiên đấu giá
     * @param bidderId ID người đặt giá
     * @param bidAmount Số tiền đặt giá
     * @param clientTimestamp Timestamp từ client (để xử lý tie-breaker)
     * @return BidResult chứa kết quả (success/failure + message)
     */
    BidResult placeBid(Long auctionId, Long bidderId, BigDecimal bidAmount, Long clientTimestamp);
}

