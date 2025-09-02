package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.SkillAuctionRequest;
import dev.lhs.charity_backend.dto.request.SkillCreationRequest;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;
import dev.lhs.charity_backend.dto.response.SkillCreationResponse;
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
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;

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
    @PostAuthorize("returnObject.username == authentication.name")
    public SkillCreationResponse createSkill(Long userId, SkillCreationRequest request) {

        User user = userRepository.findUserById(userId);
        Campaign campaign = campaignRepository.findCampaignById(request.getCampaignId());

        if (user == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);
        if (campaign == null) throw new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED);

        Skill skill = skillMapper.toSkill(request);
        skill.setUser(user);
        skill.setCampaign(campaign);
        skill.setSkillAuctions(new ArrayList<>());

        return skillMapper.toSkillResponse(skillRepository.save(skill));
    }

    @Override
    public SkillAuctionResponse auction(Long userId, Long skillId, SkillAuctionRequest request) {
        User user = userRepository.findUserById(userId);
        Skill skill = skillRepository.findSkillById(skillId);

        if (user == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);
        if (skill == null) throw new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED);

        SkillAuction skillAuction = skillAuctionMapper.toSkillAuction(request);
        if (skill.getCurentBid().compareTo(request.getBidAmount()) >= 0)
            throw new AppException(ErrorCode.INVALID_BID_PRICE);
        if (skill.getCurentBid().remainder(skill.getStepBid()).equals(BigDecimal.ZERO))
            throw new AppException(ErrorCode.INVALID_BID_PRICE);

        skill.setCurentBid(skillAuction.getBidAmount());
        skillAuction.setStatus(1);
        skillRepository.save(skill);
        skillAuctionRepository.save(skillAuction);
        return skillAuctionMapper.toSkillAuctionResponse(skillAuction, userId);
    }

}
