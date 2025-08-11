package dev.lhs.charity_backend.dto.request;

import dev.lhs.charity_backend.entity.auth.Permission;
import lombok.*;

import java.util.HashSet;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreationRequest {

    private String name;
    private String description;
    private HashSet<String> permissions = new HashSet<>(); // truyen vao ten permission

}
