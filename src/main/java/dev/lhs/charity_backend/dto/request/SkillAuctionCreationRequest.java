package dev.lhs.charity_backend.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillAuctionCreationRequest {
    
    private Long skillId;
    private Long skillOwnerId;
    private Long campaignId;
    private BigDecimal startingBid;
    private BigDecimal targetAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

