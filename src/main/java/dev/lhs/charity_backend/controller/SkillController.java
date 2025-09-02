package dev.lhs.charity_backend.controller;

import dev.lhs.charity_backend.dto.request.SkillAuctionRequest;
import dev.lhs.charity_backend.dto.request.SkillCreationRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;
import dev.lhs.charity_backend.dto.response.SkillCreationResponse;
import dev.lhs.charity_backend.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    // user create skill
    @PostMapping("/{userId}")
    ApiResponse<SkillCreationResponse> create(@PathVariable Long userId,
                                              @RequestBody SkillCreationRequest request) {
        return ApiResponse.<SkillCreationResponse>builder()
                .result(skillService.createSkill(userId, request))
                .build();
    }

    // user auc skill
    @PostMapping("/{userId}/{skillId}")
    ApiResponse<SkillAuctionResponse> auction (@PathVariable Long userId,
                                               @PathVariable Long skillId,
                                               @RequestBody SkillAuctionRequest request) {
        return ApiResponse.<SkillAuctionResponse>builder()
                .result(skillService.auction(userId, skillId, request))
                .build();
    }
}
