package dev.lhs.charity_backend.dto.request;

import dev.lhs.charity_backend.enumeration.ContentType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignContentBlockRequest {
    private ContentType contentType;
    private String content;
    private Integer position;
}
