package dev.lhs.charity_backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private String name;
    private String description;

}
