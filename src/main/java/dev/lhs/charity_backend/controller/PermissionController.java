package dev.lhs.charity_backend.controller;

import dev.lhs.charity_backend.dto.request.PermissionCreationRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.dto.response.PermissionResponse;
import dev.lhs.charity_backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    ApiResponse<PermissionResponse> create(PermissionCreationRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionService.createPermission(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAllPermissions())
                .build();
    }

    @DeleteMapping("/{permission}")
    ApiResponse<String> delete(@PathVariable String permission) {
        return ApiResponse.<String>builder()
                .result(permissionService.deletePermission(permission))
                .build();
    }
}
