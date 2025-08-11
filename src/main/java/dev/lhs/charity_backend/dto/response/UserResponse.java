package dev.lhs.charity_backend.dto.response;

import dev.lhs.charity_backend.entity.auth.Role;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String username;
    private LocalDate dob;
    private HashSet<RoleResponse> roles = new HashSet<>();

}
