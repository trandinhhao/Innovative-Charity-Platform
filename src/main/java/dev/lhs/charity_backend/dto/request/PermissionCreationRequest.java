package dev.lhs.charity_backend.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCreationRequest {

    private String name;
    private String description;

}
