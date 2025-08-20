package dev.lhs.charity_backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillCreationResponse {

    private String name;
    private String thumbnailUrl;
    private String description;
    private BigDecimal startingBid;
    private BigDecimal stepBid;
    private BigDecimal curentBid;
    private BigDecimal targetBid;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String username;
    private Long campaignId;

}
