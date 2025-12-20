package dev.lhs.charity_backend.service;

/**
 * Service xử lý finalization (kết thúc) phiên đấu giá
 * Sử dụng Delayed Message Queue để finalize đúng thời điểm
 */
public interface FinalizationService {
    
    /**
     * Xử lý finalization khi message được deliver
     * Được gọi bởi FinalizationConsumer
     * 
     * @param auctionId ID phiên đấu giá
     */
    void finalizeAuction(Long auctionId);
}

