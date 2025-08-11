package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.RoleCreationRequest;
import dev.lhs.charity_backend.dto.response.RoleResponse;
import dev.lhs.charity_backend.entity.auth.Permission;
import dev.lhs.charity_backend.entity.auth.Role;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.mapper.RoleMapper;
import dev.lhs.charity_backend.repository.PermissionRepository;
import dev.lhs.charity_backend.repository.RoleRepository;
import dev.lhs.charity_backend.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    public RoleResponse createRole(RoleCreationRequest request) {
        Role role = roleMapper.toRole(request);

        List<Permission> permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    @Override
    public String deleteRole(String role) {
        if (roleRepository.existsById(role)) {
            roleRepository.deleteById(role);
            return "Deleted successfully";
        } else throw new AppException(ErrorCode.INVALID_ROLE);
    }

}
