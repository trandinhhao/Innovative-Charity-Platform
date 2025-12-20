package dev.lhs.charity_backend.mapper;

import dev.lhs.charity_backend.dto.request.SkillAuctionRequest;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;
import dev.lhs.charity_backend.entity.SkillAuction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SkillAuctionMapper {
    @Mapping(target = "skill", ignore = true)
    @Mapping(target = "skillOwner", ignore = true)
    @Mapping(target = "campaign", ignore = true)
    @Mapping(target = "bids", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SkillAuction toSkillAuction (SkillAuctionRequest request);

    @Mapping(target = "skillId", source = "skillAuction.skill.id")
    @Mapping(target = "skillOwnerId", source = "skillAuction.skillOwner.id")
    @Mapping(target = "campaignId", source = "skillAuction.campaign.id")
    SkillAuctionResponse toSkillAuctionResponse (SkillAuction skillAuction);
}
