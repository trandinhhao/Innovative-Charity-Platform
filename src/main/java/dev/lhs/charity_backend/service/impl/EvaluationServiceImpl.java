package dev.lhs.charity_backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lhs.charity_backend.dto.response.AIAnalysisResult;
import dev.lhs.charity_backend.dto.response.VerificationResult;
import dev.lhs.charity_backend.enumeration.VerificationStatus;
import dev.lhs.charity_backend.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation của EvaluationService
 * Logic đánh giá theo đúng yêu cầu chương 2:
 * - confidenceScore < 0.70 → NEEDS_MANUAL_REVIEW
 * - meetsRequirements = false → REJECTED
 * - meetsRequirements = true && confidenceScore >= 0.90 → APPROVED
 * - meetsRequirements = true && 0.70 <= confidenceScore < 0.90 → NEEDS_MANUAL_REVIEW
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final ObjectMapper objectMapper;
    
    private static final double LOW_CONFIDENCE_THRESHOLD = 0.70;
    private static final double HIGH_CONFIDENCE_THRESHOLD = 0.90;

    @Override
    public VerificationResult evaluate(AIAnalysisResult aiResult) {
        Double confidenceScore = aiResult.getConfidenceScore() != null 
                ? aiResult.getConfidenceScore() 
                : 0.0;
        Boolean meetsRequirements = aiResult.getMeetsRequirements() != null 
                ? aiResult.getMeetsRequirements() 
                : false;

        VerificationStatus status;
        String message;
        String rejectionReason = null;

        // Logic đánh giá theo yêu cầu chương 2
        if (confidenceScore < LOW_CONFIDENCE_THRESHOLD) {
            // Confidence thấp → cần xem xét thủ công
            status = VerificationStatus.NEEDS_MANUAL_REVIEW;
            message = "Độ tin cậy thấp, cần xem xét thủ công";
            log.info("Low confidence score: {}, requiring manual review", confidenceScore);
            
        } else if (!meetsRequirements) {
            // Không đáp ứng yêu cầu → từ chối
            status = VerificationStatus.REJECTED;
            message = "Minh chứng không đáp ứng yêu cầu thử thách";
            rejectionReason = aiResult.getRejectionReason() != null 
                    ? aiResult.getRejectionReason() 
                    : "Không đáp ứng các yêu cầu của thử thách";
            log.info("Requirements not met, rejecting. Reason: {}", rejectionReason);
            
        } else if (confidenceScore >= HIGH_CONFIDENCE_THRESHOLD) {
            // Đáp ứng yêu cầu và confidence cao → phê duyệt
            status = VerificationStatus.APPROVED;
            message = "Minh chứng đã được phê duyệt";
            log.info("High confidence and requirements met, approving. Score: {}", confidenceScore);
            
        } else {
            // Đáp ứng yêu cầu nhưng confidence trung bình → cần xem xét thủ công
            status = VerificationStatus.NEEDS_MANUAL_REVIEW;
            message = "Cần xem xét thủ công để đảm bảo tính chính xác";
            log.info("Medium confidence with requirements met, requiring manual review. Score: {}", confidenceScore);
        }

        // Chuyển đổi detailedAnalysis thành JSON string
        String analysisDetails = convertAnalysisToJson(aiResult);

        return VerificationResult.builder()
                .status(status)
                .message(message)
                .confidenceScore(confidenceScore)
                .meetsRequirements(meetsRequirements)
                .rejectionReason(rejectionReason)
                .analysisDetails(analysisDetails)
                .build();
    }

    /**
     * Chuyển đổi AIAnalysisResult thành JSON string để lưu vào DB
     */
    private String convertAnalysisToJson(AIAnalysisResult aiResult) {
        try {
            return objectMapper.writeValueAsString(aiResult);
        } catch (Exception e) {
            log.error("Error converting analysis to JSON: {}", e.getMessage(), e);
            return "{}";
        }
    }
}

