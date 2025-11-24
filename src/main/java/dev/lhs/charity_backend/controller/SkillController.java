package dev.lhs.charity_backend.controller;

import dev.lhs.charity_backend.dto.request.SkillCreationRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;
import dev.lhs.charity_backend.dto.response.SkillResponse;
import dev.lhs.charity_backend.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    // user create skill
    @PostMapping("/{userId}")
    ApiResponse<SkillResponse> create(@PathVariable Long userId, @RequestBody SkillCreationRequest request) {
        return ApiResponse.<SkillResponse>builder()
                .result(skillService.createSkill(userId, request))
                .build();
    }

    @GetMapping
    ApiResponse<List<SkillResponse>> getSkills () {
        return ApiResponse.<List<SkillResponse>>builder()
                .result(skillService.getSkills())
                .build();
    }

    @GetMapping("/{skillId}")
    ApiResponse<SkillResponse> getSkill(@PathVariable Long skillId) {
        return ApiResponse.<SkillResponse>builder()
                .result(skillService.getSkill(skillId))
                .build();
    }

    @DeleteMapping("/{skillId}")
    ApiResponse<String> delete (@PathVariable Long skillId) {
        return ApiResponse.<String>builder()
                .result(skillService.deleteSkill(skillId))
                .build();
    }

    // user auc skill
    @PostMapping("/auction/{userId}/{skillId}")
    ApiResponse<SkillAuctionResponse> auction (@PathVariable Long userId,
                                               @PathVariable Long skillId,
                                               @RequestBody BigDecimal bidAmount) {
        return ApiResponse.<SkillAuctionResponse>builder()
                .result(skillService.auction(userId, skillId, bidAmount))
                .build();
    }
}
