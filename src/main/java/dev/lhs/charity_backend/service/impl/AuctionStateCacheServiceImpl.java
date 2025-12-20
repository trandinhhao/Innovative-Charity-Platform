package dev.lhs.charity_backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lhs.charity_backend.enumeration.AuctionStatus;
import dev.lhs.charity_backend.service.AuctionStateCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Implementation của AuctionStateCacheService
 * Sử dụng Redis Hash để lưu auction state
 * Key: auction:{auctionId}
 * Fields: currentBid, highestBidderId, status, endTime
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionStateCacheServiceImpl implements AuctionStateCacheService {

    private static final String AUCTION_KEY_PREFIX = "auction:";
    private static final String FIELD_CURRENT_BID = "currentBid";
    private static final String FIELD_STARTING_BID = "startingBid"; // Thêm field startingBid
    private static final String FIELD_HIGHEST_BIDDER = "highestBidderId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_END_TIME = "endTime";
    private static final long CACHE_TTL_HOURS = 24; // Cache 24 giờ

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public AuctionState getAuctionState(Long auctionId) {
        String key = AUCTION_KEY_PREFIX + auctionId;
        
        try {
            Object currentBidObj = redisTemplate.opsForHash().get(key, FIELD_CURRENT_BID);
            if (currentBidObj == null) {
                log.debug("Auction state not found in cache: {}", auctionId);
                return null;
            }
            String currentBidStr = currentBidObj.toString();
            String startingBidStr = (String) redisTemplate.opsForHash().get(key, FIELD_STARTING_BID);
            String highestBidderStr = (String) redisTemplate.opsForHash().get(key, FIELD_HIGHEST_BIDDER);
            String statusStr = (String) redisTemplate.opsForHash().get(key, FIELD_STATUS);
            String endTimeStr = (String) redisTemplate.opsForHash().get(key, FIELD_END_TIME);
            
            if (statusStr == null || endTimeStr == null) {
                log.debug("Auction state incomplete in cache: {}", auctionId);
                return null;
            }
            
            BigDecimal currentBid = new BigDecimal(currentBidStr);
            BigDecimal startingBid = startingBidStr != null ? new BigDecimal(startingBidStr) : null;
            Long highestBidderId = highestBidderStr != null ? Long.parseLong(highestBidderStr) : null;
            AuctionStatus status = AuctionStatus.valueOf(statusStr);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DATE_TIME_FORMATTER);
            
            return new AuctionState(currentBid, startingBid, highestBidderId, status, endTime);
            
        } catch (Exception e) {
            log.warn("Error getting auction state from cache: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void updateAuctionState(Long auctionId, BigDecimal currentBid, Long highestBidderId, AuctionStatus status) {
        String key = AUCTION_KEY_PREFIX + auctionId;
        
        try {
            // Giữ lại startingBid nếu đã có (không overwrite)
            // Chỉ update currentBid, highestBidderId, status
            redisTemplate.opsForHash().put(key, FIELD_CURRENT_BID, currentBid.toString());
            if (highestBidderId != null) {
                redisTemplate.opsForHash().put(key, FIELD_HIGHEST_BIDDER, highestBidderId.toString());
            } else {
                redisTemplate.opsForHash().delete(key, FIELD_HIGHEST_BIDDER);
            }
            redisTemplate.opsForHash().put(key, FIELD_STATUS, status.name());
            
            // Set TTL
            redisTemplate.expire(key, CACHE_TTL_HOURS, TimeUnit.HOURS);
            
            log.debug("Updated auction state in cache: auctionId={}, currentBid={}, highestBidderId={}", 
                    auctionId, currentBid, highestBidderId);
            
        } catch (Exception e) {
            log.error("Error updating auction state in cache: {}", e.getMessage(), e);
        }
    }

    @Override
    public void finalizeAuctionState(Long auctionId, Long winnerId, BigDecimal finalBid) {
        String key = AUCTION_KEY_PREFIX + auctionId;
        
        try {
            redisTemplate.opsForHash().put(key, FIELD_CURRENT_BID, finalBid.toString());
            if (winnerId != null) {
                redisTemplate.opsForHash().put(key, FIELD_HIGHEST_BIDDER, winnerId.toString());
            }
            redisTemplate.opsForHash().put(key, FIELD_STATUS, AuctionStatus.COMPLETED.name());
            
            // Set TTL
            redisTemplate.expire(key, CACHE_TTL_HOURS, TimeUnit.HOURS);
            
            log.info("Finalized auction state in cache: auctionId={}, winnerId={}, finalBid={}", 
                    auctionId, winnerId, finalBid);
            
        } catch (Exception e) {
            log.error("Error finalizing auction state in cache: {}", e.getMessage(), e);
        }
    }

    @Override
    public void initializeAuctionState(Long auctionId, BigDecimal startingBid, LocalDateTime endTime, AuctionStatus status) {
        String key = AUCTION_KEY_PREFIX + auctionId;
        
        try {
            // Lưu cả startingBid và currentBid = 0 (vì chưa có ai đấu giá)
            redisTemplate.opsForHash().put(key, FIELD_STARTING_BID, startingBid.toString());
            redisTemplate.opsForHash().put(key, FIELD_CURRENT_BID, BigDecimal.ZERO.toString());
            redisTemplate.opsForHash().put(key, FIELD_STATUS, status.name());
            redisTemplate.opsForHash().put(key, FIELD_END_TIME, endTime.format(DATE_TIME_FORMATTER));
            
            // Set TTL
            redisTemplate.expire(key, CACHE_TTL_HOURS, TimeUnit.HOURS);
            
            log.debug("Initialized auction state in cache: auctionId={}, startingBid={}, currentBid=0, endTime={}", 
                    auctionId, startingBid, endTime);
            
        } catch (Exception e) {
            log.error("Error initializing auction state in cache: {}", e.getMessage(), e);
        }
    }

    @Override
    public void updateAuctionStatus(Long auctionId, AuctionStatus status) {
        String key = AUCTION_KEY_PREFIX + auctionId;
        
        try {
            redisTemplate.opsForHash().put(key, FIELD_STATUS, status.name());
            // Set TTL
            redisTemplate.expire(key, CACHE_TTL_HOURS, TimeUnit.HOURS);
            
            log.debug("Updated auction status in cache: auctionId={}, status={}", auctionId, status);
            
        } catch (Exception e) {
            log.error("Error updating auction status in cache: {}", e.getMessage(), e);
        }
    }

    @Override
    public void evictAuctionState(Long auctionId) {
        String key = AUCTION_KEY_PREFIX + auctionId;
        redisTemplate.delete(key);
        log.debug("Evicted auction state from cache: {}", auctionId);
    }
}

