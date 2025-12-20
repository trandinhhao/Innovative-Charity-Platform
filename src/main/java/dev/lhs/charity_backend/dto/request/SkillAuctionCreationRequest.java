package dev.lhs.charity_backend.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO để tạo phiên đấu giá mới
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillAuctionCreationRequest {
    
    private Long skillId; // Kỹ năng được đấu giá
    
    private Long skillOwnerId; // Người cung cấp kỹ năng
    
    private Long campaignId; // Chiến dịch từ thiện nhận tiền
    
    private BigDecimal startingBid; // Giá khởi điểm
    
    private BigDecimal targetAmount; // Mức mong muốn (optional)
    
    private LocalDateTime startTime; // Thời gian bắt đầu
    
    private LocalDateTime endTime; // Thời gian kết thúc
}

