package dev.lhs.charity_backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private String name;
    private String description;
    private String avatarUrl;
    private String email;
    private String phoneNumber;
    private String address;

}
