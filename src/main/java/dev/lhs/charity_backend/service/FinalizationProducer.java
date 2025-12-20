package dev.lhs.charity_backend.service;

import java.time.LocalDateTime;

/**
 * Producer để schedule delayed message cho finalization
 */
public interface FinalizationProducer {
    
    /**
     * Schedule finalization message với delay
     * @param auctionId ID phiên đấu giá
     * @param endTime Thời điểm kết thúc
     */
    void scheduleFinalization(Long auctionId, LocalDateTime endTime);
}

