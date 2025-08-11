package dev.lhs.charity_backend.dto.response;

import lombok.*;

import java.util.HashSet;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private String name;
    private String description;
    private HashSet<PermissionResponse> permissions = new HashSet<>();

}

