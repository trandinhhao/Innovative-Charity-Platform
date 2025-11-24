package dev.lhs.charity_backend.mapper;

import dev.lhs.charity_backend.dto.request.SkillAuctionRequest;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;
import dev.lhs.charity_backend.entity.SkillAuction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SkillAuctionMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "skill", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "bidTime", ignore = true)
    SkillAuction toSkillAuction (SkillAuctionRequest request);

    @Mapping(target = "userAucId", expression = "java(userAucId)")
    @Mapping(target = "skillId", source = "skillAuction.skill.id")
    SkillAuctionResponse toSkillAuctionResponse (SkillAuction skillAuction, Long userAucId);
}
