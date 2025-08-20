package dev.lhs.charity_backend.dto.request;

import jakarta.persistence.Column;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillCreationRequest {

    private String name;
    private String thumbnailUrl;
    private String description;
    private BigDecimal startingBid;
    private BigDecimal stepBid;
    private BigDecimal curentBid;
    private BigDecimal targetBid;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long campaignId;

}
