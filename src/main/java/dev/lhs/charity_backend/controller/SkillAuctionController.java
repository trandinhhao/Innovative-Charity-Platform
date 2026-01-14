package dev.lhs.charity_backend.controller;

import dev.lhs.charity_backend.dto.request.SkillAuctionCreationRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.dto.response.BidResponse;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;
import dev.lhs.charity_backend.service.SkillAuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class SkillAuctionController { // auction management

    private final SkillAuctionService skillAuctionService;

    @PostMapping
    ApiResponse<SkillAuctionResponse> createSkillWithAuction(@RequestBody SkillAuctionCreationRequest request) {
        return ApiResponse.<SkillAuctionResponse>builder()
                .result(skillAuctionService.createSkillWithAuction(request))
                .build();
    }

    @GetMapping("/{auctionId}")
    ApiResponse<SkillAuctionResponse> getAuction(@PathVariable Long auctionId) {
        return ApiResponse.<SkillAuctionResponse>builder()
                .result(skillAuctionService.getAuction(auctionId))
                .build();
    }

    @GetMapping
    ApiResponse<List<SkillAuctionResponse>> listAuctions() {
        return ApiResponse.<List<SkillAuctionResponse>>builder()
                .result(skillAuctionService.listAuctions())
                .build();
    }

    @GetMapping("/active")
    ApiResponse<List<SkillAuctionResponse>> listActiveAuctions() {
        return ApiResponse.<List<SkillAuctionResponse>>builder()
                .result(skillAuctionService.listActiveAuctions())
                .build();
    }

    @GetMapping("/campaign/{campaignId}")
    ApiResponse<List<SkillAuctionResponse>> listAuctionsByCampaign(@PathVariable Long campaignId) {
        return ApiResponse.<List<SkillAuctionResponse>>builder()
                .result(skillAuctionService.listAuctionsByCampaign(campaignId))
                .build();
    }

    // auction -> queue
    // return response message only
    @PostMapping("/{auctionId}/bids")
    ApiResponse<String> placeBidForSkill(@PathVariable Long auctionId,
                                         @RequestParam BigDecimal bidAmount) {
        return ApiResponse.<String>builder()
                .result(skillAuctionService.placeBidForSkill(auctionId, bidAmount))
                .build();
    }

    // check highest after bid
    @GetMapping("/{auctionId}/my-bid")
    ApiResponse<BidResponse> checkBidStatus(@PathVariable Long auctionId) {
        return ApiResponse.<BidResponse>builder()
                .result(skillAuctionService.checkBidStatus(auctionId))
                .build();
    }

     // sort by cost higher to lower
    @GetMapping("/{auctionId}/bids")
    ApiResponse<List<BidResponse>> getAuctionBids(@PathVariable Long auctionId) {
        return ApiResponse.<List<BidResponse>>builder()
                .result(skillAuctionService.getAuctionBids(auctionId))
                .build();
    }

    @GetMapping("/history")
    ApiResponse<List<BidResponse>> getMyBidHistory() {
        return ApiResponse.<List<BidResponse>>builder()
                .result(skillAuctionService.getMyBidHistory())
                .build();
    }
}

