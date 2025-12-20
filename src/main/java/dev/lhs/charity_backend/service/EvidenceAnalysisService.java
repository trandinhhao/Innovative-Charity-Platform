package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.response.AIAnalysisResult;
import dev.lhs.charity_backend.entity.Challenge;

/**
 * Service tích hợp với Spring AI để phân tích minh chứng
 * Tầng tích hợp AI theo yêu cầu chương 2
 */
public interface EvidenceAnalysisService {
    
    /**
     * Gửi ảnh và yêu cầu thử thách đến AI để phân tích
     * @param imageUrl URL ảnh từ Cloudinary
     * @param challenge Challenge entity chứa requirements
     * @return AIAnalysisResult chứa kết quả phân tích từ AI
     */
    AIAnalysisResult analyzeEvidence(String imageUrl, Challenge challenge);
}

