package dev.lhs.charity_backend.dto.response;

import dev.lhs.charity_backend.entity.UserChallenge;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeResponse {
    private String id;
    private String name;
    private String thumbnailUrl;
    private String description;
    private BigDecimal unitAmount;
    private BigDecimal goalAmount;
    private BigDecimal currentAmount; // Số tiền hiện tại = số lượng APPROVED * unitAmount
    private List<UserChallenge> userChallenges = new ArrayList<>();
    private Long userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long campaignId;
}
