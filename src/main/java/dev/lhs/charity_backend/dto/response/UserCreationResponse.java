package dev.lhs.charity_backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationResponse {

    private String id;
    private String username;
    private String role;

}
