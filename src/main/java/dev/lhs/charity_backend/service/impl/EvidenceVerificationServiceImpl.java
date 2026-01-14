package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.response.AIAnalysisResult;
import dev.lhs.charity_backend.dto.response.VerificationResult;
import dev.lhs.charity_backend.entity.Challenge;
import dev.lhs.charity_backend.entity.User;
import dev.lhs.charity_backend.entity.UserChallenge;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.enumeration.VerificationStatus;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.repository.UserChallengeRepository;
import dev.lhs.charity_backend.service.EvaluationService;
import dev.lhs.charity_backend.service.EvidenceAnalysisService;
import dev.lhs.charity_backend.service.EvidenceVerificationService;
import dev.lhs.charity_backend.service.ImagePreprocessingService;
import dev.lhs.charity_backend.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation của EvidenceVerificationService
 * Thực hiện pipeline 7 bước:
 * 1. User thực hiện thử thách (đã có)
 * 2. User chụp ảnh (đã có)
 * 3. Upload ảnh lên Cloudinary → lưu URL
 * 4. Tiền xử lý ảnh (resize/nén/trích metadata)
 * 5. Gửi ảnh + requirements đến AI
 * 6. So khớp kết quả AI → tính confidenceScore → quyết định status
 * 7. Lưu kết quả vào DB và trả về
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenceVerificationServiceImpl implements EvidenceVerificationService {

    private final ImageService imageService;
    private final ImagePreprocessingService imagePreprocessingService;
    private final EvidenceAnalysisService evidenceAnalysisService;
    private final EvaluationService evaluationService;
    private final UserChallengeRepository userChallengeRepository;

    @Override
    @Async("evidenceVerificationExecutor")
    @Transactional
    public CompletableFuture<UserChallenge> verifyEvidenceAsync(User user, Challenge challenge, MultipartFile imageFile) {
        log.info("Starting async evidence verification for user {} and challenge {}", user.getId(), challenge.getId());
        
        try {
            UserChallenge result = verifyEvidenceSync(user, challenge, imageFile);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Error in async evidence verification: {}", e.getMessage(), e);
            // Cập nhật status thành REJECTED nếu có lỗi
            UserChallenge userChallenge = createUserChallengeWithError(user, challenge, imageFile, e.getMessage());
            return CompletableFuture.completedFuture(userChallenge);
        }
    }

    @Override
    @Transactional
    public UserChallenge verifyEvidenceSync(User user, Challenge challenge, MultipartFile imageFile) {
        log.info("Starting evidence verification pipeline for user {} and challenge {}", user.getId(), challenge.getId());
        
        // Bước 3: Upload ảnh lên Cloudinary và lưu URL
        log.info("Step 3: Uploading image to Cloudinary...");
        String imageUrl = imageService.uploadImage(imageFile);
        log.info("Image uploaded successfully. URL: {}", imageUrl);
        
        // Tạo UserChallenge với status PROCESSING
        UserChallenge userChallenge = UserChallenge.builder()
                .proofImageUrl(imageUrl)
                .user(user)
                .challenge(challenge)
                .verificationStatus(VerificationStatus.PROCESSING)
                .status(VerificationStatus.PROCESSING.getCode())
                .isMatch(false) // Tạm thời, sẽ cập nhật sau
                .submitTime(LocalDateTime.now())
                .build();
        
        userChallenge = userChallengeRepository.save(userChallenge);
        log.info("UserChallenge created with ID: {} and status: PROCESSING", userChallenge.getId());
        
        try {
            // Bước 4: preprocessing (resize/nén/trích metadata)
            log.info("Step 4: Preprocessing image...");
            Map<String, Object> metadata = imagePreprocessingService.preprocessImage(imageFile);
            log.info("Image preprocessing completed. Metadata: {}", metadata);
            
            // Bước 5: send to AI
            log.info("Step 5: Sending to AI for analysis...");
            AIAnalysisResult aiResult = evidenceAnalysisService.analyzeEvidence(imageUrl, challenge);
            log.info("AI analysis completed. Confidence: {}, Meets requirements: {}", 
                    aiResult.getConfidenceScore(), aiResult.getMeetsRequirements());
            
            // Bước 6: Đánh giá kết quả AI và quyết định status
            log.info("Step 6: Evaluating AI result...");
            VerificationResult verificationResult = evaluationService.evaluate(aiResult);
            log.info("Evaluation completed. Status: {}, Confidence: {}", 
                    verificationResult.getStatus(), verificationResult.getConfidenceScore());
            
            // Bước 7: Cập nhật UserChallenge với kết quả
            log.info("Step 7: Updating UserChallenge with results...");
            userChallenge.setVerificationStatus(verificationResult.getStatus());
            userChallenge.setStatus(verificationResult.getStatus().getCode());
            userChallenge.setIsMatch(verificationResult.getMeetsRequirements());
            userChallenge.setConfidenceScore(verificationResult.getConfidenceScore());
            userChallenge.setAnalysisDetails(verificationResult.getAnalysisDetails());
            userChallenge.setRejectionReason(verificationResult.getRejectionReason());
            userChallenge.setMessage(verificationResult.getMessage());
            userChallenge.setProcessedAt(LocalDateTime.now());
            
            userChallenge = userChallengeRepository.save(userChallenge);
            log.info("UserChallenge updated successfully. Final status: {}", userChallenge.getVerificationStatus());
            
            return userChallenge;
            
        } catch (Exception e) {
            log.error("Error during verification pipeline: {}", e.getMessage(), e);
            
            // Cập nhật status thành REJECTED nếu có lỗi
            userChallenge.setVerificationStatus(VerificationStatus.REJECTED);
            userChallenge.setStatus(VerificationStatus.REJECTED.getCode());
            userChallenge.setRejectionReason("Lỗi trong quá trình xử lý: " + e.getMessage());
            userChallenge.setMessage("Đã xảy ra lỗi khi xử lý minh chứng");
            userChallenge.setProcessedAt(LocalDateTime.now());
            
            return userChallengeRepository.save(userChallenge);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserChallenge getVerificationStatus(Long userChallengeId) {
        return userChallengeRepository.findById(userChallengeId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_CHALLENGE_NOT_FOUND));
    }

    private UserChallenge createUserChallengeWithError(User user, Challenge challenge, MultipartFile imageFile, String errorMessage) {
        try {
            String imageUrl = imageService.uploadImage(imageFile);
            
            UserChallenge userChallenge = UserChallenge.builder()
                    .proofImageUrl(imageUrl)
                    .user(user)
                    .challenge(challenge)
                    .verificationStatus(VerificationStatus.REJECTED)
                    .status(VerificationStatus.REJECTED.getCode())
                    .isMatch(false)
                    .rejectionReason("Lỗi trong quá trình xử lý: " + errorMessage)
                    .message("Đã xảy ra lỗi khi xử lý minh chứng")
                    .submitTime(LocalDateTime.now())
                    .processedAt(LocalDateTime.now())
                    .build();
            
            return userChallengeRepository.save(userChallenge);
        } catch (Exception e) {
            log.error("Error creating UserChallenge with error status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create UserChallenge", e);
        }
    }
}

