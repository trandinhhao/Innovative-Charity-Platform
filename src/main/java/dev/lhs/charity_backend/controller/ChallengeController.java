package dev.lhs.charity_backend.controller;

import dev.lhs.charity_backend.dto.request.ChallengeCreationRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.dto.response.ChallengeResponse;
import dev.lhs.charity_backend.dto.response.UserChallengeResponse;
import dev.lhs.charity_backend.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/challenges")
@RequiredArgsConstructor
public class ChallengeController {
    private final ChallengeService challengeService;

    @PostMapping()
    ApiResponse<ChallengeResponse> create(@RequestBody ChallengeCreationRequest request) {
        return ApiResponse.<ChallengeResponse>builder()
                .result(challengeService.createChallenge(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<ChallengeResponse>> getChallenges() {
        return ApiResponse.<List<ChallengeResponse>>builder()
                .result(challengeService.getChallenges())
                .build();
    }

    @GetMapping("/{challengeId}")
    ApiResponse<ChallengeResponse> getChallenge(@PathVariable Long challengeId) {
        return ApiResponse.<ChallengeResponse>builder()
                .result(challengeService.getChallenge(challengeId))
                .build();
    }

    @DeleteMapping("/{challengeId}")
    ApiResponse<String> delete (@PathVariable Long challengeId) {
        return ApiResponse.<String>builder()
                .result(challengeService.deleteChallenge(challengeId))
                .build();
    }

    @PostMapping("/{challengeId}/submit")
    ApiResponse<UserChallengeResponse> submitProof (@PathVariable Long challengeId,
                                                    @RequestParam("file")MultipartFile file) {
        return ApiResponse.<UserChallengeResponse>builder()
                .result(challengeService.submitProof(challengeId, file))
                .build();
    }

    @GetMapping("/verification/{userChallengeId}")
    ApiResponse<UserChallengeResponse> getVerificationStatus(@PathVariable Long userChallengeId) {
        return ApiResponse.<UserChallengeResponse>builder()
                .result(challengeService.getVerificationStatus(userChallengeId))
                .build();
    }

    @GetMapping("/history")
    ApiResponse<List<UserChallengeResponse>> getUserChallengeHistory() {
        return ApiResponse.<List<UserChallengeResponse>>builder()
                .result(challengeService.getUserChallengeHistory())
                .build();
    }
}
