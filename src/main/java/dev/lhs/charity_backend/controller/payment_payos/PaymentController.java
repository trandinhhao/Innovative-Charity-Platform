package dev.lhs.charity_backend.controller.payment_payos;

import dev.lhs.charity_backend.dto.payment_payos.PayOSApiResponse;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    private final PayOS payOS;

    public PaymentController(PayOS payOS) {
        super();
        this.payOS = payOS;
    }

    @PostMapping(path = "/payos_transfer_handler")
    public PayOSApiResponse<WebhookData> payosTransferHandler(@RequestBody Object body)
            throws JsonProcessingException, IllegalArgumentException {
        try {
            WebhookData data = payOS.webhooks().verify(body);
            System.out.println(data);
            return PayOSApiResponse.success("Webhook delivered", data);
        } catch (Exception e) {
            e.printStackTrace();
            return PayOSApiResponse.error(e.getMessage());
        }
    }
}