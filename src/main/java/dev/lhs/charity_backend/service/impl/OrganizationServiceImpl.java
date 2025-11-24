package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.OrganizationRequest;
import dev.lhs.charity_backend.dto.response.OrganizationResponse;
import dev.lhs.charity_backend.entity.Organization;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.mapper.OrganizationMapper;
import dev.lhs.charity_backend.repository.OrganizationRepository;
import dev.lhs.charity_backend.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizationResponse create(OrganizationRequest request) {
        Organization organization = organizationMapper.toOrganization(request);

        organization.setCampaigns(new ArrayList<>());
        return organizationMapper.toOrganizationResponse(organizationRepository.save(organization));
    }

    @Override
    public List<OrganizationResponse> getOrganizations() {
        return organizationRepository.findAll()
                .stream().map(organizationMapper::toOrganizationResponse).toList();
    }

    @Override
    public OrganizationResponse getOrganization(Long orgId) {
        if (organizationRepository.existsById(orgId)) {
            return organizationMapper.toOrganizationResponse(organizationRepository.findOrganizationById(orgId));
        } else throw new AppException(ErrorCode.ORGANIZATION_NOT_EXISTED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(Long orgId) {
        if (organizationRepository.existsById(orgId)) {
            organizationRepository.deleteById(orgId);
            return "Organization has been deleted";
        } else throw new AppException(ErrorCode.ORGANIZATION_NOT_EXISTED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizationResponse update(Long orgId, OrganizationRequest request) {
        Organization organization = organizationRepository.findOrganizationById(orgId);
        if (organization == null) throw new AppException(ErrorCode.ORGANIZATION_NOT_EXISTED);

        organizationMapper.updateOrganization(organization, request);
        return organizationMapper.toOrganizationResponse(organizationRepository.save(organization));
    }

}
