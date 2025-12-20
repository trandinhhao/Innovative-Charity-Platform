package dev.lhs.charity_backend.dto.response;

import dev.lhs.charity_backend.enumeration.AuctionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO response cho SkillAuction
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillAuctionResponse {
    
    private Long id;
    
    private BigDecimal startingBid;
    
    private BigDecimal currentBid;
    
    private BigDecimal targetAmount;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private AuctionStatus status;
    
    private Integer statusCode;
    
    private Long highestBidderId;
    
    private Long skillId;
    
    private Long skillOwnerId;
    
    private Long campaignId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
