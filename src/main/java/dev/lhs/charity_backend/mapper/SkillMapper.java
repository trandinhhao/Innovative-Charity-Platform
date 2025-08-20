package dev.lhs.charity_backend.mapper;

import dev.lhs.charity_backend.dto.request.SkillCreationRequest;
import dev.lhs.charity_backend.dto.response.SkillCreationResponse;
import dev.lhs.charity_backend.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    @Mapping(target = "campaign", ignore = true)
    Skill toSkill(SkillCreationRequest request);

    @Mapping(target = "username", source = "user.username") // skill.user.username
    @Mapping(target = "campaignId", source = "campaign.id") // skill.campaign.id
    SkillCreationResponse toSkillResponse (Skill skill);
}
