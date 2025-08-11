package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.RoleCreationRequest;
import dev.lhs.charity_backend.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {

    RoleResponse createRole(RoleCreationRequest request);
    List<RoleResponse> getAllRoles();
    String deleteRole(String role);
}
