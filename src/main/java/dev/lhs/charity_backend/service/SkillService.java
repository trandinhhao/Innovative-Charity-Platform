package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.SkillAuctionRequest;
import dev.lhs.charity_backend.dto.request.SkillCreationRequest;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;
import dev.lhs.charity_backend.dto.response.SkillCreationResponse;

public interface SkillService {
    SkillCreationResponse createSkill(Long userId, SkillCreationRequest request);

    SkillAuctionResponse auction(Long userId, Long skillId, SkillAuctionRequest request);
}
