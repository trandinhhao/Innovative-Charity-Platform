package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.response.VerificationResult;
import dev.lhs.charity_backend.entity.Challenge;
import dev.lhs.charity_backend.entity.User;
import dev.lhs.charity_backend.entity.UserChallenge;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

/**
 * Service chính để xử lý pipeline 7 bước cho Evidence Verification
 * Orchestrate toàn bộ logic từ upload → AI analysis → evaluation → DB update
 */
public interface EvidenceVerificationService {
    
    /**
     * Xử lý minh chứng theo pipeline 7 bước (async)
     * Bước 1-3: User đã thực hiện, upload ảnh
     * Bước 4-7: Hệ thống xử lý
     * 
     * @param user User thực hiện thử thách
     * @param challenge Challenge cần verify
     * @param imageFile File ảnh minh chứng
     * @return CompletableFuture<UserChallenge> với status PROCESSING ban đầu
     */
    CompletableFuture<UserChallenge> verifyEvidenceAsync(User user, Challenge challenge, MultipartFile imageFile);
    
    /**
     * Xử lý minh chứng theo pipeline 7 bước (sync - cho testing hoặc internal use)
     * 
     * @param user User thực hiện thử thách
     * @param challenge Challenge cần verify
     * @param imageFile File ảnh minh chứng
     * @return UserChallenge với kết quả đã được xử lý
     */
    UserChallenge verifyEvidence(User user, Challenge challenge, MultipartFile imageFile);
    
    /**
     * Lấy trạng thái verification của UserChallenge
     * 
     * @param userChallengeId ID của UserChallenge
     * @return UserChallenge với trạng thái mới nhất
     */
    UserChallenge getVerificationStatus(Long userChallengeId);
}

