package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.response.AIAnalysisResult;
import dev.lhs.charity_backend.dto.response.VerificationResult;
import dev.lhs.charity_backend.enumeration.VerificationStatus;

/**
 * Service đánh giá kết quả AI và quyết định status
 * Logic đánh giá theo yêu cầu chương 2
 */
public interface EvaluationService {
    
    /**
     * Đánh giá kết quả AI và quyết định verification status
     * @param aiResult Kết quả phân tích từ AI
     * @return VerificationResult với status, message, và details
     */
    VerificationResult evaluate(AIAnalysisResult aiResult);
}

