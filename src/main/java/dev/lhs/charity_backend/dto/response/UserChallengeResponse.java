package dev.lhs.charity_backend.dto.response;

import dev.lhs.charity_backend.enumeration.VerificationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserChallengeResponse {
    private Long id;
    private String proofImageUrl;
    private LocalDateTime submitTime;
    private String message;
    private Boolean isMatch;
    private Integer status;
    private VerificationStatus verificationStatus;
    private Double confidenceScore;
    private String analysisDetails;
    private String rejectionReason;
    private LocalDateTime processedAt;
    private Long userId;
    private Long challengeId;
}
