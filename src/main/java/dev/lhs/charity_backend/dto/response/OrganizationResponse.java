package dev.lhs.charity_backend.dto.response;

import dev.lhs.charity_backend.entity.Campaign;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    private String id;
    private String name;
    private String description;
    private String avatarUrl;
    private String email;
    private String phoneNumber;
    private String address;
    private List<Campaign> campaigns;
}
