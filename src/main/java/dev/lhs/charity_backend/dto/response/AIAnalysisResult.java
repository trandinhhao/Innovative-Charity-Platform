package dev.lhs.charity_backend.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResult {

    private List<String> objectsDetected;
    private List<String> actionsDetected;
    private String context;
    private Boolean meetsRequirements;
    private Double confidenceScore; // 0.0 -> 1.0
    private Map<String, RequirementAnalysis> detailedAnalysis; // <requirement, <met, explanation>
    private List<String> fraudIndicators; // kiem tra gian lan
    private String rejectionReason;

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

