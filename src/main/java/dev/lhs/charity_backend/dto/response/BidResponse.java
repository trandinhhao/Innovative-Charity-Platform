package dev.lhs.charity_backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {
    
    private Long id;
    private BigDecimal bidAmount;
    private LocalDateTime bidTime;
    private Long bidderId;
    private String bidderUsername; // Optional
    private Long auctionId;
    private Boolean isHighestBid;
}
