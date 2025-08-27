package dev.lhs.charity_backend.controller;

import dev.lhs.charity_backend.dto.request.CampaignRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.dto.response.CampaignResponse;
import dev.lhs.charity_backend.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    ApiResponse<CampaignResponse> create (@RequestBody CampaignRequest request) {
        return ApiResponse.<CampaignResponse>builder()
                .result(campaignService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<CampaignResponse>> getCampaigns () {
        return ApiResponse.<List<CampaignResponse>>builder()
                .result(campaignService.getCampaigns())
                .build();
    }

    @GetMapping("/{campId}")
    ApiResponse<CampaignResponse> getCampaign(@PathVariable Long campId) {
        return ApiResponse.<CampaignResponse>builder()
                .result(campaignService.getCampaign(campId))
                .build();
    }

    @DeleteMapping("/{campId}")
    ApiResponse<String> delete(@PathVariable Long campId) {
        return ApiResponse.<String>builder()
                .result(campaignService.delete(campId))
                .build();
    }

    @PutMapping("/{campId}")
    ApiResponse<CampaignResponse> update (@PathVariable Long campId,
                                          @RequestBody CampaignRequest request) {
        return ApiResponse.<CampaignResponse>builder()
                .result(campaignService.update(campId, request))
                .build();
    }
}
