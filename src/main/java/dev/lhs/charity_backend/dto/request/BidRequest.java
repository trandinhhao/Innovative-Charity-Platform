package dev.lhs.charity_backend.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidRequest {
    
    private Long auctionId;
    private Long bidderId;
    private BigDecimal bidAmount;
    private Long clientTimestamp; // Timestamp từ client để xử lý tie-breaker
}

