package dev.lhs.charity_backend.mapper;

import dev.lhs.charity_backend.dto.request.OrganizationRequest;
import dev.lhs.charity_backend.dto.response.OrganizationResponse;
import dev.lhs.charity_backend.entity.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {
    Organization toOrganization(OrganizationRequest request);
    OrganizationResponse toOrganizationResponse (Organization organization);

    @Mapping(target = "campaigns", ignore = true)
    void updateOrganization(@MappingTarget Organization organization, OrganizationRequest request);
}
