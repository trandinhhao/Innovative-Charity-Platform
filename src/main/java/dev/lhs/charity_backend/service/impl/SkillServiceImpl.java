package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.SkillCreationRequest;
import dev.lhs.charity_backend.dto.response.SkillCreationResponse;
import dev.lhs.charity_backend.entity.Campaign;
import dev.lhs.charity_backend.entity.Skill;
import dev.lhs.charity_backend.entity.User;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.mapper.SkillMapper;
import dev.lhs.charity_backend.repository.CampaignRepository;
import dev.lhs.charity_backend.repository.SkillRepository;
import dev.lhs.charity_backend.repository.UserRepository;
import dev.lhs.charity_backend.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;

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
}
