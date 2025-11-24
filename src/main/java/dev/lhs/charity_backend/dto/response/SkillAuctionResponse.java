package dev.lhs.charity_backend.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillAuctionResponse {
    private BigDecimal bidAmount;
//    private String note;
//    private Integer status;
    private Long userAucId;
    private Long skillId;
}
