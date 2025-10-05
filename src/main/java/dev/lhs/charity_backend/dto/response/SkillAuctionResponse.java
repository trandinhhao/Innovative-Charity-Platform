package dev.lhs.charity_backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SkillAuctionResponse {
    private BigDecimal bidAmount;
    private String note;
    private Integer status;
    private Long userAucId;
    private Long skillId;
}
