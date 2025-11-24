package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.SkillCreationRequest;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;
import dev.lhs.charity_backend.dto.response.SkillResponse;
import dev.lhs.charity_backend.entity.Campaign;
import dev.lhs.charity_backend.entity.Skill;
import dev.lhs.charity_backend.entity.SkillAuction;
import dev.lhs.charity_backend.entity.User;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.mapper.SkillAuctionMapper;
import dev.lhs.charity_backend.mapper.SkillMapper;
import dev.lhs.charity_backend.repository.CampaignRepository;
import dev.lhs.charity_backend.repository.SkillAuctionRepository;
import dev.lhs.charity_backend.repository.SkillRepository;
import dev.lhs.charity_backend.repository.UserRepository;
import dev.lhs.charity_backend.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final SkillAuctionMapper skillAuctionMapper;
    private final SkillAuctionRepository skillAuctionRepository;

    @Override
//    @PostAuthorize("returnObject.username == authentication.name")
    public SkillResponse createSkill(Long userId, SkillCreationRequest request) {

        User user = userRepository.findUserById(userId);
        Campaign campaign = campaignRepository.findCampaignById(request.getCampaignId());

        if (user == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);
        if (campaign == null) throw new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED);

        Skill skill = skillMapper.toSkill(request);
        skill.setCurentBid(skill.getStartingBid());
        skill.setUser(user);
        skill.setCampaign(campaign);
        skill.setSkillAuctions(new ArrayList<>());

        return skillMapper.toSkillResponse(skillRepository.save(skill));
    }

    @Override
    public List<SkillResponse> getSkills() {
        return skillRepository.findAll()
                .stream().map(skillMapper::toSkillResponse).toList();
    }

    @Override
    public SkillResponse getSkill(Long skillId) {
        if (skillRepository.existsById(skillId)) {
            return skillMapper.toSkillResponse(skillRepository.findSkillById(skillId));
        } else throw new AppException(ErrorCode.SKILL_NOT_EXISTED);
    }

    @Override
    public String deleteSkill(Long skillId) {
        if (skillRepository.existsById(skillId)) {
            skillRepository.deleteById(skillId);
            return "Skill has been deleted";
        } else throw new AppException(ErrorCode.SKILL_NOT_EXISTED);
    }

    @Override
    public SkillAuctionResponse auction(Long userId, Long skillId, BigDecimal bidAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_EXISTED));

        BigDecimal currentBid = skill.getCurentBid();

        if (bidAmount.compareTo(currentBid) <= 0 || bidAmount.compareTo(skill.getTargetBid()) > 0)
            throw new AppException(ErrorCode.INVALID_BID_PRICE);

        SkillAuction skillAuction = SkillAuction.builder()
                .skill(skill)
                .user(user)
                .bidAmount(bidAmount)
                .status(1)
                .build();

        skill.setCurentBid(skillAuction.getBidAmount());
        skillRepository.save(skill);
        skillAuctionRepository.save(skillAuction);
        return skillAuctionMapper.toSkillAuctionResponse(skillAuction, userId);
    }

}
