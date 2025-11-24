package dev.lhs.charity_backend.dto.request;

import dev.lhs.charity_backend.entity.CampaignContentBlock;
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
public class CampaignRequest {

    private String name;
    private String thumbnailUrl;
    private String description;
    private BigDecimal goalAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long orgId;
    private List<CampaignContentBlockRequest> campaignContentBlocks = new ArrayList<>();

}
