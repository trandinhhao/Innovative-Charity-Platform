package dev.lhs.charity_backend.dto.response;

import dev.lhs.charity_backend.entity.SkillAuction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {
    private String id;
    private String name;
    private String thumbnailUrl;
    private String description;
    private BigDecimal startingBid;
//    private BigDecimal stepBid;
    private BigDecimal curentBid;
    private BigDecimal targetBid;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long userId;
    private Long campaignId;
    private List<SkillAuction> skillAuctions= new ArrayList<>();
}
