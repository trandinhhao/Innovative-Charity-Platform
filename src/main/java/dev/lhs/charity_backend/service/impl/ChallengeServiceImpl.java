package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.ChallengeCreationRequest;
import dev.lhs.charity_backend.dto.response.ChallengeResponse;
import dev.lhs.charity_backend.dto.response.UserChallengeResponse;
import dev.lhs.charity_backend.entity.Campaign;
import dev.lhs.charity_backend.entity.Challenge;
import dev.lhs.charity_backend.entity.User;
import dev.lhs.charity_backend.entity.UserChallenge;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.enumeration.VerificationStatus;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.mapper.ChallengeMapper;
import dev.lhs.charity_backend.repository.CampaignRepository;
import dev.lhs.charity_backend.repository.ChallengeRepository;
import dev.lhs.charity_backend.repository.UserChallengeRepository;
import dev.lhs.charity_backend.repository.UserRepository;
import dev.lhs.charity_backend.service.ChallengeService;
import dev.lhs.charity_backend.service.ChatService;
import dev.lhs.charity_backend.service.ImageService;
import dev.lhs.charity_backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final ChallengeMapper challengeMapper;
    private final ImageService imageService;
    private final ChatService chatService;
    private final UserChallengeRepository userChallengeRepository;
    private final dev.lhs.charity_backend.service.EvidenceVerificationService evidenceVerificationService;

    @Override
    public ChallengeResponse createChallenge(ChallengeCreationRequest request) {
        User user = userRepository.findUserById(SecurityUtils.getUserId());
        Campaign campaign = campaignRepository.findCampaignById(request.getCampaignId());

        if (user == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);
        if (campaign == null) throw new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED);

        Challenge challenge = challengeMapper.toChallenge(request);
        challenge.setCurrentAmount(BigDecimal.ZERO);
        challenge.setUserChallenges(new ArrayList<>());
        challenge.setUser(user);
        challenge.setCampaign(campaign);

        return challengeMapper.toChallengeResponse(challengeRepository.save(challenge));
    }

    @Override
    public List<ChallengeResponse> getChallenges() {
        return challengeRepository.findAll()
                .stream()
                .map(challenge -> {
                    ChallengeResponse response = challengeMapper.toChallengeResponse(challenge);
                    calculateCurrentAmount(response, challenge);
                    return response;
                })
                .toList();
    }

    @Override
    public ChallengeResponse getChallenge(Long challengeId) {
        if (challengeRepository.existsById(challengeId)) {
            Challenge challenge = challengeRepository.findChallengeById(challengeId);
            ChallengeResponse response = challengeMapper.toChallengeResponse(challenge);
            calculateCurrentAmount(response, challenge);
            return response;
        } else throw new AppException(ErrorCode.CHALLENGE_NOT_EXISTED);
    }

    // currentAmount = APPROVED * unitAmount
    private void calculateCurrentAmount(ChallengeResponse response, Challenge challenge) {
        if (challenge.getUserChallenges() == null || challenge.getUnitAmount() == null) {
            response.setCurrentAmount(BigDecimal.ZERO);
            return;
        }

        long approvedCount = challenge.getUserChallenges().stream()
                .filter(uc -> uc.getVerificationStatus() == VerificationStatus.APPROVED)
                .count();

        BigDecimal currentAmount = challenge.getUnitAmount().multiply(BigDecimal.valueOf(approvedCount));
        response.setCurrentAmount(currentAmount);
    }

    @Override
    public String deleteChallenge(Long challengeId) {
        if (challengeRepository.existsById(challengeId)) {
            challengeRepository.deleteById(challengeId);
            return "Challenge has been deleted";
        } else throw new AppException(ErrorCode.CHALLENGE_NOT_EXISTED);
    }

    @Override
    public UserChallengeResponse submitProof(Long challengeId, MultipartFile file) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new AppException(ErrorCode.CHALLENGE_NOT_EXISTED));

        // async processing -> return PROCESSING status immediately
        //evidenceVerificationService.verifyEvidenceAsync(user, challenge, file);

        // sync processing: wait for async processing to finish
        UserChallenge userChallenge = evidenceVerificationService.verifyEvidenceSync(user, challenge, file);

        return challengeMapper.toUserChallengeResponse(userChallenge);
    }

    @Override
    public UserChallengeResponse getVerificationStatus(Long userChallengeId) {
        UserChallenge userChallenge = evidenceVerificationService.getVerificationStatus(userChallengeId);
        return challengeMapper.toUserChallengeResponse(userChallenge);
    }

    @Override
    public List<UserChallengeResponse> getUserChallengeHistory() {
        // Validate user exists

        Long userId = SecurityUtils.getUserId();
        if (userId == null) throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);

        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        
        List<UserChallenge> userChallenges = userChallengeRepository.findAllByUserIdOrderBySubmitTimeDesc(userId);
        return userChallenges.stream()
                .map(challengeMapper::toUserChallengeResponse)
                .toList();
    }
}
