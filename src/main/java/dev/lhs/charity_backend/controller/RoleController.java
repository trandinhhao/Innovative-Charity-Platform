package dev.lhs.charity_backend.controller;

import dev.lhs.charity_backend.dto.request.RoleCreationRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.dto.response.RoleResponse;
import dev.lhs.charity_backend.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleCreationRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.createRole(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAllRoles())
                .build();
    }

    @DeleteMapping("/{role}")
    ApiResponse<String> delete(@PathVariable String role) {
        return ApiResponse.<String>builder()
                .result(roleService.deleteRole(role))
                .build();
    }

}
