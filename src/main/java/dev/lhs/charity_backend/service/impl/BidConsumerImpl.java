package dev.lhs.charity_backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lhs.charity_backend.dto.request.BidRequest;
import dev.lhs.charity_backend.dto.response.BidResult;
import dev.lhs.charity_backend.enumeration.AuctionStatus;
import dev.lhs.charity_backend.service.AuctionStateCacheService;
import dev.lhs.charity_backend.service.BidConsumer;
import dev.lhs.charity_backend.service.BidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementation của BidConsumer
 * Xử lý bid request từ queue:
 * 1. Parse message
 * 2. Validate sơ bộ với Redis (reject nhanh nếu không hợp lệ)
 * 3. Xử lý với DB + Pessimistic Lock
 * 4. Update Redis nếu thành công
 * 5. Publish event để UI update real-time
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BidConsumerImpl implements BidConsumer {

    private final ObjectMapper objectMapper;
    private final BidService bidService;
    private final AuctionStateCacheService auctionStateCacheService;

    @Override
    @RabbitListener(queues = "${rabbitmq.queue.bid}")
    public void processBidRequest(String message) {
        log.info("Received bid request from queue: {}", message);
        
        try {
            // Parse message
            BidRequest bidRequest = objectMapper.readValue(message, BidRequest.class);
            
            // Bước 1: Validate sơ bộ với Redis (filter nhanh)
            AuctionStateCacheService.AuctionState state = auctionStateCacheService.getAuctionState(bidRequest.getAuctionId());
            
            if (state != null) {
                // Reject nhanh nếu status != ACTIVE
                if (state.getStatus() != AuctionStatus.ACTIVE) {
                    log.warn("Auction not ACTIVE (from cache): auctionId={}, status={}", 
                            bidRequest.getAuctionId(), state.getStatus());
                    return; // Reject, không xử lý tiếp
                }
                
                // Reject nhanh nếu đã hết hạn
                if (LocalDateTime.now().isAfter(state.getEndTime())) {
                    log.warn("Auction expired (from cache): auctionId={}, endTime={}", 
                            bidRequest.getAuctionId(), state.getEndTime());
                    return; // Reject
                }
                
                // Validate bid amount theo logic mới:
                // - Nếu currentBid = 0: bidAmount phải >= startingBid
                // - Nếu currentBid > 0: bidAmount phải > currentBid
                BigDecimal currentBid = state.getCurrentBid();
                BigDecimal startingBid = state.getStartingBid();
                boolean isValidBid;
                
                if (currentBid != null && currentBid.compareTo(BigDecimal.ZERO) == 0) {
                    // Chưa có ai đấu giá: phải đặt >= startingBid
                    if (startingBid == null) {
                        log.warn("StartingBid not found in cache, skipping cache validation: auctionId={}", 
                                bidRequest.getAuctionId());
                        // Không reject, để DB validate
                    } else {
                        isValidBid = bidRequest.getBidAmount().compareTo(startingBid) >= 0;
                        if (!isValidBid) {
                            log.warn("Bid amount too low (from cache): auctionId={}, bidAmount={}, startingBid={}", 
                                    bidRequest.getAuctionId(), bidRequest.getBidAmount(), startingBid);
                            return; // Reject
                        }
                    }
                } else {
                    // Đã có người đấu giá: phải đặt > currentBid
                    isValidBid = bidRequest.getBidAmount().compareTo(currentBid) > 0;
                    if (!isValidBid) {
                        log.warn("Bid amount too low (from cache): auctionId={}, bidAmount={}, currentBid={}", 
                                bidRequest.getAuctionId(), bidRequest.getBidAmount(), currentBid);
                        return; // Reject
                    }
                }
            }
            
            // Bước 2: Xử lý với DB + Pessimistic Lock (xác nhận chính thức)
            BidResult result = bidService.placeBid(
                    bidRequest.getAuctionId(),
                    bidRequest.getBidderId(),
                    bidRequest.getBidAmount(),
                    bidRequest.getClientTimestamp()
            );
            
            // Bước 3: Update Redis nếu thành công
            if (result.isSuccess()) {
                auctionStateCacheService.updateAuctionState(
                        bidRequest.getAuctionId(),
                        result.getCurrentBid(),
                        result.getHighestBidderId(),
                        AuctionStatus.ACTIVE
                );
                
                log.info("Bid processed successfully: auctionId={}, bidderId={}, amount={}", 
                        bidRequest.getAuctionId(), bidRequest.getBidderId(), bidRequest.getBidAmount());
                
                // TODO: Publish event ra Redis Pub/Sub hoặc WebSocket để UI update real-time
                // publishBidUpdateEvent(bidRequest.getAuctionId(), result);
                
            } else {
                log.warn("Bid rejected: auctionId={}, reason={}", bidRequest.getAuctionId(), result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error processing bid request: {}", e.getMessage(), e);
            // TODO: Có thể đẩy vào dead letter queue nếu cần
        }
    }
}

