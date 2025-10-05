package dev.lhs.charity_backend.dto.response;

import dev.lhs.charity_backend.entity.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {

    private String name;
    private String thumbnailUrl;
    private String description;
    private BigDecimal goalAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<CampaignContentBlock> campaignContentBlocks;
    private List<Skill> skills;
    private List<Challenge> challenges;
//    private Organization organization;
    private Long organizationId;
    private List<CampaignComment> campaignComments;

}
