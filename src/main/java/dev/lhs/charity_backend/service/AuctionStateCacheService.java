package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.enumeration.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service quản lý cache auction state trong Redis
 * Key structure: auction:{auctionId}
 * Fields: currentBid, highestBidderId, status, endTime
 */
public interface AuctionStateCacheService {
    
    /**
     * Lấy state từ Redis (để filter nhanh trước khi vào DB)
     */
    AuctionState getAuctionState(Long auctionId);
    
    /**
     * Cập nhật state vào Redis sau khi bid thành công
     */
    void updateAuctionState(Long auctionId, BigDecimal currentBid, Long highestBidderId, AuctionStatus status);
    
    /**
     * Initialize auction state vào Redis khi tạo auction
     */
    void initializeAuctionState(Long auctionId, BigDecimal startingBid, LocalDateTime endTime, AuctionStatus status);
    
    /**
     * Cập nhật state khi finalize auction
     */
    void finalizeAuctionState(Long auctionId, Long winnerId, BigDecimal finalBid);
    
    /**
     * Chỉ cập nhật status của auction (dùng khi activate PENDING → ACTIVE)
     */
    void updateAuctionStatus(Long auctionId, AuctionStatus status);
    
    /**
     * Xóa cache khi auction bị xóa
     */
    void evictAuctionState(Long auctionId);
    
    /**
     * Inner class chứa auction state
     */
    class AuctionState {
        private BigDecimal currentBid;
        private BigDecimal startingBid; // Thêm startingBid để validate khi currentBid = 0
        private Long highestBidderId;
        private AuctionStatus status;
        private LocalDateTime endTime;
        
        public AuctionState() {}
        
        public AuctionState(BigDecimal currentBid, BigDecimal startingBid, Long highestBidderId, AuctionStatus status, LocalDateTime endTime) {
            this.currentBid = currentBid;
            this.startingBid = startingBid;
            this.highestBidderId = highestBidderId;
            this.status = status;
            this.endTime = endTime;
        }
        
        // Getters and Setters
        public BigDecimal getCurrentBid() { return currentBid; }
        public void setCurrentBid(BigDecimal currentBid) { this.currentBid = currentBid; }
        
        public BigDecimal getStartingBid() { return startingBid; }
        public void setStartingBid(BigDecimal startingBid) { this.startingBid = startingBid; }
        
        public Long getHighestBidderId() { return highestBidderId; }
        public void setHighestBidderId(Long highestBidderId) { this.highestBidderId = highestBidderId; }
        
        public AuctionStatus getStatus() { return status; }
        public void setStatus(AuctionStatus status) { this.status = status; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }
}

