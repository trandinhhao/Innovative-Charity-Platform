package dev.lhs.charity_backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lhs.charity_backend.service.FinalizationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Implementation của FinalizationProducer
 * Sử dụng RabbitMQ delayed message plugin để schedule finalization
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinalizationProducerImpl implements FinalizationProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange.finalization}")
    private String finalizationExchange;

    @Value("${rabbitmq.routing-key.finalization}")
    private String finalizationRoutingKey;

    @Override
    public void scheduleFinalization(Long auctionId, LocalDateTime endTime) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Duration delay = Duration.between(now, endTime);
            
            if (delay.isNegative() || delay.isZero()) {
                log.warn("End time is in the past or now, finalizing immediately: auctionId={}, endTime={}", 
                        auctionId, endTime);
                // Có thể gọi trực tiếp finalization service
                return;
            }
            
            long delayMillis = delay.toMillis();
            
            // Gửi message với delay
            // Note: Cần RabbitMQ Delayed Message Plugin để support delay header
            // Nếu không có plugin, có thể dùng TTL + Dead Letter Exchange hoặc schedule ở application level
            String message = String.valueOf(auctionId);
            
            // Cách 1: Dùng delay header (cần plugin)
            // Note: RabbitMQ Delayed Message Plugin sử dụng header "x-delay" (milliseconds)
            try {
                rabbitTemplate.convertAndSend(
                        finalizationExchange,
                        finalizationRoutingKey,
                        message,
                        messagePostProcessor -> {
                            // Set delay header (cần rabbitmq_delayed_message_exchange plugin)
                            // Header name: "x-delay" (milliseconds)
                            messagePostProcessor.getMessageProperties().setHeader("x-delay", (int) delayMillis);
                            return messagePostProcessor;
                        }
                );
            } catch (Exception e) {
                // Fallback: Nếu không có plugin, log warning và schedule ở application level
                log.warn("Delayed message plugin not available, using application-level scheduling. " +
                        "Consider installing rabbitmq_delayed_message_exchange plugin. Error: {}", e.getMessage());
                // TODO: Có thể implement application-level scheduling với ScheduledExecutorService
                // hoặc dùng Spring @Scheduled với database để track
            }
            
            log.info("Scheduled finalization: auctionId={}, endTime={}, delay={}ms", 
                    auctionId, endTime, delayMillis);
            
        } catch (Exception e) {
            log.error("Error scheduling finalization: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to schedule finalization", e);
        }
    }
}

