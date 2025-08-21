package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.OrganizationRequest;
import dev.lhs.charity_backend.dto.response.OrganizationResponse;

import java.util.List;

public interface OrganizationService {

    OrganizationResponse create(OrganizationRequest request);
    List<OrganizationResponse> getOrganizations();
    OrganizationResponse getOrganization(Long orgId);
    String delete(Long orgId);
    OrganizationResponse update(Long orgId, OrganizationRequest request);
}
