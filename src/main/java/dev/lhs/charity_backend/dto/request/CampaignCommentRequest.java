package dev.lhs.charity_backend.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCommentRequest {
    private Long userId;
    private String content;
}
