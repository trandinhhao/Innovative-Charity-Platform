package dev.lhs.charity_backend.dto.request;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO để tạo SkillAuction và đặt giá sau khi thanh toán PayOS thành công
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuctionAndBidRequest {

    private Long skillId; // Kỹ năng được đấu giá
    private BigDecimal bidAmount; // Giá tiền đã thanh toán (đơn vị nghìn)
}

