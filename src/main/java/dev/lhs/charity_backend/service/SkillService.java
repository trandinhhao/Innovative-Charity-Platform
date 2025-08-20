package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.SkillCreationRequest;
import dev.lhs.charity_backend.dto.response.SkillCreationResponse;

public interface SkillService {
    SkillCreationResponse createSkill(Long userId, SkillCreationRequest request);
}
