package dev.lhs.charity_backend.dto.response;

import dev.lhs.charity_backend.enumeration.VerificationStatus;
import lombok.*;

/**
 * Kết quả đánh giá minh chứng sau khi xử lý qua pipeline
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResult {
    
    private VerificationStatus status;
    
    private String message;
    
    private Double confidenceScore;
    
    private Boolean meetsRequirements;
    
    private String rejectionReason;
    
    private String analysisDetails; // JSON string
}

