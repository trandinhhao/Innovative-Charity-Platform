package dev.lhs.charity_backend.service.scheduler;

import dev.lhs.charity_backend.service.SkillAuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler để tự động quản lý lifecycle của auction:
 * 1. Activate các auction PENDING khi đến thời gian bắt đầu
 * 2. Disable các auction ACTIVE đã quá thời gian kết thúc
 * Chạy mỗi phút để check và cập nhật status
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionActivationScheduler {

    private final SkillAuctionService skillAuctionService;

    /**
     * Chạy mỗi phút để check và activate các auction PENDING đã đến startTime
     * Cron: 0 * * * * ? = mỗi phút tại giây 0
     */
    @Scheduled(cron = "0 * * * * ?")
    public void activatePendingAuctions() {
        try {
            log.debug("Running scheduled task to activate pending auctions");
            skillAuctionService.activatePendingAuctions();
        } catch (Exception e) {
            log.error("Error in scheduled task to activate pending auctions: {}", e.getMessage(), e);
        }
    }

    /**
     * Chạy mỗi phút để check và disable các auction ACTIVE đã quá endTime
     * Cron: 0 * * * * ? = mỗi phút tại giây 0
     */
    @Scheduled(cron = "0 * * * * ?")
    public void disableExpiredAuctions() {
        try {
            log.debug("Running scheduled task to disable expired auctions");
            skillAuctionService.disableExpiredAuctions();
        } catch (Exception e) {
            log.error("Error in scheduled task to disable expired auctions: {}", e.getMessage(), e);
        }
    }
}
