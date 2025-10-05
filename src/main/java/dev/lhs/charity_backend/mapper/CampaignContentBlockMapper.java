package dev.lhs.charity_backend.mapper;

import dev.lhs.charity_backend.dto.request.CampaignContentBlockRequest;
import dev.lhs.charity_backend.dto.request.CampaignContentBlockUpdateRequest;
import dev.lhs.charity_backend.entity.CampaignContentBlock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampaignContentBlockMapper {
    @Mapping(target = "campaign", ignore = true)
    CampaignContentBlock toCampaignContentBlock (CampaignContentBlockUpdateRequest request);
}
