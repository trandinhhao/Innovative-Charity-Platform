package dev.lhs.charity_backend.mapper;

import dev.lhs.charity_backend.dto.request.RoleCreationRequest;
import dev.lhs.charity_backend.dto.response.RoleResponse;
import dev.lhs.charity_backend.entity.auth.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    Role toRole (RoleCreationRequest request);

    RoleResponse toRoleResponse(Role role);
}
