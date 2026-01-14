package dev.lhs.charity_backend.dto.response;

import dev.lhs.charity_backend.entity.*;
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
public class CampaignResponse {
    private String id;
    private String name;
    private String thumbnailUrl;
    private String description;
    private BigDecimal goalAmount;
    private BigDecimal raisedAmount; // = tổng challenges.currentAmount + tổng skills.curentBid
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<CampaignContentBlock> campaignContentBlocks = new ArrayList<>();
    private List<Skill> skills = new ArrayList<>();
    private List<Challenge> challenges = new ArrayList<>();
//    private Organization organization;
    private Long organizationId;
    private List<CampaignComment> campaignComments = new ArrayList<>();

}
