package dev.lhs.charity_backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCommentResponse {
    private Long id;
    private Long userId;
    private Long campaignId;
    private String content;
}
