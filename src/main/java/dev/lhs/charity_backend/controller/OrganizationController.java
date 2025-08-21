package dev.lhs.charity_backend.controller;

import dev.lhs.charity_backend.dto.request.OrganizationRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.dto.response.OrganizationResponse;
import dev.lhs.charity_backend.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    ApiResponse<OrganizationResponse> create (@RequestBody OrganizationRequest request) {
        return ApiResponse.<OrganizationResponse>builder()
                .result(organizationService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<OrganizationResponse>> getOrganizations () {
        return ApiResponse.<List<OrganizationResponse>>builder()
                .result(organizationService.getOrganizations())
                .build();
    }

    @GetMapping("/{orgId}")
    ApiResponse<OrganizationResponse> getOrganization (@PathVariable Long orgId) {
        return ApiResponse.<OrganizationResponse>builder()
                .result(organizationService.getOrganization(orgId))
                .build();
    }

    @DeleteMapping("/{orgId}")
    ApiResponse<String> delete (@PathVariable Long orgId) {
        return ApiResponse.<String>builder()
                .result(organizationService.delete(orgId))
                .build();
    }

    @PutMapping("/{orgId}")
    ApiResponse<OrganizationResponse> update (@PathVariable Long orgId,
                                              @RequestBody OrganizationRequest request) {
        return ApiResponse.<OrganizationResponse>builder()
                .result(organizationService.update(orgId, request))
                .build();
    }
}
