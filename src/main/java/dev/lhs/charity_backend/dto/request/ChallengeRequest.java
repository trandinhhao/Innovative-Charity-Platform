package dev.lhs.charity_backend.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeRequest {
    private String name;
    private String thumbnailUrl;
    private String description;
    private BigDecimal unitAmount;
    private BigDecimal goalAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
