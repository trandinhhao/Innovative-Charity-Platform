package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.ChallengeCreationRequest;
import dev.lhs.charity_backend.dto.response.ChallengeResponse;
import dev.lhs.charity_backend.dto.response.UserChallengeResponse;
import dev.lhs.charity_backend.dto.response.UserSubmitResponse;
import dev.lhs.charity_backend.entity.Campaign;
import dev.lhs.charity_backend.entity.Challenge;
import dev.lhs.charity_backend.entity.User;
import dev.lhs.charity_backend.entity.UserChallenge;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.mapper.ChallengeMapper;
import dev.lhs.charity_backend.repository.CampaignRepository;
import dev.lhs.charity_backend.repository.ChallengeRepository;
import dev.lhs.charity_backend.repository.UserChallengeRepository;
import dev.lhs.charity_backend.repository.UserRepository;
import dev.lhs.charity_backend.service.ChallengeService;
import dev.lhs.charity_backend.service.ChatService;
import dev.lhs.charity_backend.service.ImageService;
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

    @Override
    public ChallengeResponse createChallenge(Long userId, ChallengeCreationRequest request) {
        User user = userRepository.findUserById(userId);
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
                .stream().map(challengeMapper::toChallengeResponse).toList();
    }

    @Override
    public ChallengeResponse getChallenge(Long challengeId) {
        if (challengeRepository.existsById(challengeId)) {
            return challengeMapper.toChallengeResponse(challengeRepository.findChallengeById(challengeId));
        } else throw new AppException(ErrorCode.CHALLENGE_NOT_EXISTED);
    }

    @Override
    public String deleteChallenge(Long challengeId) {
        if (challengeRepository.existsById(challengeId)) {
            challengeRepository.deleteById(challengeId);
            return "Challenge has been deleted";
        } else throw new AppException(ErrorCode.CHALLENGE_NOT_EXISTED);
    }

    @Override
    public UserChallengeResponse submitProof(Long userId, Long challengeId, MultipartFile file) {
        String secure_url = imageService.uploadImage(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new AppException(ErrorCode.CHALLENGE_NOT_EXISTED));

        UserSubmitResponse response = chatService.checkSubmitProof(file, challenge.getDescription());

        UserChallenge userChallenge = UserChallenge.builder()
                .proofImageUrl(secure_url)
                .message(response.getMessage())
                .user(user)
                .challenge(challenge)
                .status(1)
                .isMatch(response.getIsMatch().equals("YES") ? true : false)
                .build();

        return challengeMapper.toUserChallengeResponse(userChallengeRepository.save(userChallenge));
    }
}
