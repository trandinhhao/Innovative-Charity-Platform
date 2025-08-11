package dev.lhs.charity_backend.mapper;

import dev.lhs.charity_backend.dto.request.PermissionCreationRequest;
import dev.lhs.charity_backend.dto.response.PermissionResponse;
import dev.lhs.charity_backend.entity.auth.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionCreationRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
