package dev.lhs.charity_backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO response cho Bid information
 */
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
    
    private String bidderUsername; // Optional: có thể thêm nếu cần
    
    private Long auctionId;
    
    private Boolean isHighestBid; // Có phải bid cao nhất không
}
