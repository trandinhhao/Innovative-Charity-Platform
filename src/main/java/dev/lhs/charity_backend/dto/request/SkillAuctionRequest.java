package dev.lhs.charity_backend.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillAuctionRequest {
    private BigDecimal bidAmount;
    private String note;
}
