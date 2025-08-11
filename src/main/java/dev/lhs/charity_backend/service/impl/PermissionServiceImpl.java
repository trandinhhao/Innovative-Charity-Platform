package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.PermissionCreationRequest;
import dev.lhs.charity_backend.dto.response.PermissionResponse;
import dev.lhs.charity_backend.entity.auth.Permission;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.mapper.PermissionMapper;
import dev.lhs.charity_backend.repository.PermissionRepository;
import dev.lhs.charity_backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public PermissionResponse createPermission(PermissionCreationRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(permissionMapper::toPermissionResponse)
                .toList();
    }

    @Override
    public String deletePermission(String permission) {
        if (permissionRepository.existsById(permission)) {
            permissionRepository.deleteById(permission);
            return "Deleted successfully";
        } else throw new AppException(ErrorCode.INVALID_PERMISSION);
    }


}
