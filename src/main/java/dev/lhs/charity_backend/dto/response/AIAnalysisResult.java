package dev.lhs.charity_backend.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * POJO để map JSON response từ AI analysis
 * Tương ứng với output format yêu cầu trong chương 2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResult {
    
    /**
     * Danh sách các đối tượng được phát hiện trong ảnh
     */
    private List<String> objectsDetected;
    
    /**
     * Danh sách các hành động được phát hiện
     */
    private List<String> actionsDetected;
    
    /**
     * Bối cảnh của ảnh (ví dụ: ngoài trời, trong nhà, v.v.)
     */
    private String context;
    
    /**
     * Có đáp ứng yêu cầu thử thách hay không
     */
    private Boolean meetsRequirements;
    
    /**
     * Điểm tin cậy từ 0.0 đến 1.0
     */
    private Double confidenceScore;
    
    /**
     * Phân tích chi tiết từng requirement
     * Key: requirement description
     * Value: {met: boolean, explanation: string}
     */
    private Map<String, RequirementAnalysis> detailedAnalysis;
    
    /**
     * Các chỉ số gian lận được phát hiện
     */
    private List<String> fraudIndicators;
    
    /**
     * Lý do từ chối (nếu có)
     */
    private String rejectionReason;
    
    /**
     * Nested class cho requirement analysis
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequirementAnalysis {
        private Boolean met;
        private String explanation;
    }
}

