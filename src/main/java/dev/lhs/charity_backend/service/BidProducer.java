package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.BidRequest;

/**
 * Service đẩy bid request vào Message Queue
 * Decouple giữa API và xử lý bid
 */
public interface BidProducer {
    
    /**
     * Đẩy bid request vào queue
     * @param bidRequest Bid request từ client
     */
    void sendBidRequest(BidRequest bidRequest);
}

