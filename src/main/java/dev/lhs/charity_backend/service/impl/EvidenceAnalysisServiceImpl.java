package dev.lhs.charity_backend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lhs.charity_backend.dto.response.AIAnalysisResult;
import dev.lhs.charity_backend.entity.Challenge;
import dev.lhs.charity_backend.service.EvidenceAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.Map;

/**
 * Implementation của EvidenceAnalysisService
 * Sử dụng Spring AI để gửi prompt + image đến AI API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenceAnalysisServiceImpl implements EvidenceAnalysisService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;
    
    /**
     * Tạo ChatClient riêng cho evidence verification (không cần memory)
     */
    private ChatClient getChatClient() {
        return chatClientBuilder.build();
    }

    @Override
    public AIAnalysisResult analyzeEvidence(String imageUrl, Challenge challenge) {
        try {
            // Tạo prompt từ challenge requirements
            String prompt = buildPrompt(challenge);
            
            // Tải ảnh từ URL
            Resource imageResource = loadImageFromUrl(imageUrl);
            
            // Cấu hình ChatOptions cho evidence verification
            ChatOptions chatOptions = ChatOptions.builder()
                    .temperature(0.0) // Độ ổn định cao, không sáng tạo
                    .build();

            // Gọi AI với prompt và image
            log.info("Sending evidence analysis request for challenge ID: {}", challenge.getId());
            
            ChatClient chatClient = getChatClient();
            AIAnalysisResult result = chatClient.prompt()
                    .options(chatOptions)
                    .system(buildSystemPrompt())
                    .user(userSpec -> userSpec
                            .media(org.springframework.ai.content.Media.builder()
                                    .mimeType(org.springframework.util.MimeType.valueOf("image/jpeg")) // Hoặc detect từ URL
                                    .data(imageResource)
                                    .build())
                            .text(prompt))
                    .call()
                    .entity(new ParameterizedTypeReference<AIAnalysisResult>() {});

            log.info("AI analysis completed. Confidence score: {}, Meets requirements: {}", 
                    result.getConfidenceScore(), result.getMeetsRequirements());
            
            return result;

        } catch (Exception e) {
            log.error("Error analyzing evidence with AI: {}", e.getMessage(), e);
            // Trả về kết quả mặc định với confidence thấp
            return AIAnalysisResult.builder()
                    .meetsRequirements(false)
                    .confidenceScore(0.0)
                    .rejectionReason("Lỗi khi phân tích bằng AI: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Xây dựng system prompt theo đúng tinh thần chương 2
     */
    private String buildSystemPrompt() {
        return """
                Bạn là một chuyên gia đánh giá minh chứng từ thiện, nghiêm ngặt và khách quan.
                Nhiệm vụ của bạn là phân tích ảnh minh chứng và đánh giá xem nó có đáp ứng đầy đủ các yêu cầu của thử thách từ thiện hay không.
                
                Bạn phải:
                1. Phân tích chi tiết từng yêu cầu trong thử thách
                2. Kiểm tra xem ảnh có chứa đầy đủ các yếu tố được yêu cầu không
                3. Đánh giá tính xác thực của minh chứng (không phải ảnh fake, photoshop, hoặc không liên quan)
                4. Đưa ra điểm confidence score từ 0.0 đến 1.0 dựa trên mức độ chắc chắn của bạn
                5. Phát hiện các dấu hiệu gian lận nếu có
                
                Bạn phải trả về kết quả dưới dạng JSON chính xác theo format được yêu cầu.
                """;
    }

    /**
     * Xây dựng prompt từ challenge requirements
     */
    private String buildPrompt(Challenge challenge) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("YÊU CẦU THỬ THÁCH:\n");
        promptBuilder.append("Tên thử thách: ").append(challenge.getName()).append("\n\n");
        promptBuilder.append("Mô tả chi tiết:\n");
        promptBuilder.append(challenge.getDescription()).append("\n\n");
        
        promptBuilder.append("Hãy phân tích ảnh minh chứng và trả về kết quả theo format JSON sau:\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"objectsDetected\": [\"danh sách các đối tượng được phát hiện\"],\n");
        promptBuilder.append("  \"actionsDetected\": [\"danh sách các hành động được phát hiện\"],\n");
        promptBuilder.append("  \"context\": \"bối cảnh của ảnh (ngoài trời/trong nhà/v.v.)\",\n");
        promptBuilder.append("  \"meetsRequirements\": true/false,\n");
        promptBuilder.append("  \"confidenceScore\": 0.0-1.0,\n");
        promptBuilder.append("  \"detailedAnalysis\": {\n");
        promptBuilder.append("    \"requirement_1\": {\"met\": true/false, \"explanation\": \"giải thích\"},\n");
        promptBuilder.append("    \"requirement_2\": {\"met\": true/false, \"explanation\": \"giải thích\"}\n");
        promptBuilder.append("  },\n");
        promptBuilder.append("  \"fraudIndicators\": [\"các dấu hiệu gian lận nếu có\"],\n");
        promptBuilder.append("  \"rejectionReason\": \"lý do từ chối nếu không đáp ứng yêu cầu\"\n");
        promptBuilder.append("}\n");
        
        return promptBuilder.toString();
    }

    /**
     * Tải ảnh từ URL (Cloudinary)
     */
    private Resource loadImageFromUrl(String imageUrl) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            byte[] imageBytes = restTemplate.getForObject(new URL(imageUrl).toURI(), byte[].class);
            if (imageBytes == null) {
                throw new RuntimeException("Failed to load image from URL: " + imageUrl);
            }
            return new ByteArrayResource(imageBytes);
        } catch (Exception e) {
            log.error("Error loading image from URL: {}", imageUrl, e);
            throw new RuntimeException("Cannot load image from URL: " + imageUrl, e);
        }
    }
}

