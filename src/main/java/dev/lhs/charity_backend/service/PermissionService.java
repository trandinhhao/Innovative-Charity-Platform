package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.PermissionCreationRequest;
import dev.lhs.charity_backend.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {

    PermissionResponse createPermission(PermissionCreationRequest request);
    List<PermissionResponse> getAllPermissions();
    String deletePermission(String permission);
}
