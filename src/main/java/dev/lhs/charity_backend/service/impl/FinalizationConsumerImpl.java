package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.service.FinalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Consumer xử lý finalization message từ delayed queue
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinalizationConsumerImpl {

    private final FinalizationService finalizationService;

    @RabbitListener(queues = "${rabbitmq.queue.finalization}")
    public void processFinalization(String message) {
        try {
            Long auctionId = Long.parseLong(message);
            log.info("Processing finalization message: auctionId={}", auctionId);
            finalizationService.finalizeAuction(auctionId);
        } catch (Exception e) {
            log.error("Error processing finalization message: {}", e.getMessage(), e);
        }
    }
}

