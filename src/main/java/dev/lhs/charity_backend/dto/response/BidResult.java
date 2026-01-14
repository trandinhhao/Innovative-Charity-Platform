package dev.lhs.charity_backend.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResult {
    
    private boolean success;
    private String message;
    private BigDecimal currentBid;
    private Long highestBidderId;
    private Long bidId; // ID của bid record nếu thành công
}

