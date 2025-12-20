package dev.lhs.charity_backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lhs.charity_backend.dto.request.BidRequest;
import dev.lhs.charity_backend.service.BidProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementation của BidProducer
 * Sử dụng RabbitMQ để đẩy bid request vào queue
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BidProducerImpl implements BidProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange.bid}")
    private String bidExchange;

    @Value("${rabbitmq.routing-key.bid}")
    private String bidRoutingKey;

    @Override
    public void sendBidRequest(BidRequest bidRequest) {
        try {
            String message = objectMapper.writeValueAsString(bidRequest);
            rabbitTemplate.convertAndSend(bidExchange, bidRoutingKey, message);
            log.info("Sent bid request to queue: auctionId={}, bidderId={}, amount={}", 
                    bidRequest.getAuctionId(), bidRequest.getBidderId(), bidRequest.getBidAmount());
        } catch (Exception e) {
            log.error("Error sending bid request to queue: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send bid request to queue", e);
        }
    }
}

