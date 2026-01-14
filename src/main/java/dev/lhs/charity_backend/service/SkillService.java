package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.SkillCreationRequest;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;
import dev.lhs.charity_backend.dto.response.SkillResponse;

import java.math.BigDecimal;
import java.util.List;

public interface SkillService {
    SkillResponse createSkill(Long userId, SkillCreationRequest request);
    List<SkillResponse> getSkills();
    SkillResponse getSkill(Long skillId);
    String deleteSkill(Long skillId);
    SkillAuctionResponse auction(Long userId, Long skillId, BigDecimal bidAmount);
    SkillAuctionResponse createAuctionAndBid(Long userId, Long skillId, BigDecimal bidAmount);
}
