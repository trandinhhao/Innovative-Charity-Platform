package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.ChallengeCreationRequest;
import dev.lhs.charity_backend.dto.response.ChallengeResponse;
import dev.lhs.charity_backend.dto.response.UserChallengeResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChallengeService {
    ChallengeResponse createChallenge(Long userId, ChallengeCreationRequest request);
    List<ChallengeResponse> getChallenges();
    ChallengeResponse getChallenge(Long challengeId);
    String deleteChallenge(Long challengeId);
    UserChallengeResponse submitProof(Long userId, Long challengeId, MultipartFile file);
    UserChallengeResponse getVerificationStatus(Long userChallengeId);
}
