package dev.lhs.charity_backend.mapper;

import dev.lhs.charity_backend.dto.request.CampaignRequest;
import dev.lhs.charity_backend.dto.request.CampaignUpdateRequest;
import dev.lhs.charity_backend.dto.response.CampaignResponse;
import dev.lhs.charity_backend.entity.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CampaignMapper {
    @Mapping(target = "organization", ignore = true)
    Campaign toCampaign (CampaignRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    CampaignResponse toCampaignResponse (Campaign campaign);

    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "challenges", ignore = true)
    @Mapping(target = "campaignComments", ignore = true)
//    @Mapping(target = "campaignContentBlocks", ignore = true)
    void updateCampaign(@MappingTarget Campaign campaign, CampaignUpdateRequest request);
}
