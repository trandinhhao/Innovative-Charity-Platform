package dev.lhs.charity_backend.controller.payment_payos;

import java.util.Map;

import dev.lhs.charity_backend.dto.payment_payos.CreatePaymentLinkRequestBody;
import dev.lhs.charity_backend.dto.payment_payos.PayOSApiResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.payos.PayOS;
import vn.payos.core.FileDownloadResponse;
import vn.payos.exception.APIException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.invoices.InvoicesInfo;
import vn.payos.model.webhooks.ConfirmWebhookResponse;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final PayOS payOS;

    public OrderController(PayOS payOS) {
        super();
        this.payOS = payOS;
    }

    @PostMapping(path = "/create")
    public PayOSApiResponse<CreatePaymentLinkResponse> createPaymentLink(
            @RequestBody CreatePaymentLinkRequestBody RequestBody) {
        try {
            final String productName = RequestBody.getProductName();
            final String description = RequestBody.getDescription();
            final String returnUrl = RequestBody.getReturnUrl();
            final String cancelUrl = RequestBody.getCancelUrl();
            final long price = RequestBody.getPrice();
            long orderCode = System.currentTimeMillis() / 1000;
            PaymentLinkItem item =
                    PaymentLinkItem.builder()
                            .name(productName)
                            .quantity(1)
                            .price(price)
                            .build();

            CreatePaymentLinkRequest paymentData =
                    CreatePaymentLinkRequest.builder()
                            .orderCode(orderCode)
                            .description(description)
                            .amount(price)
                            .item(item)
                            .returnUrl(returnUrl)
                            .cancelUrl(cancelUrl)
                            .build();

            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);
            return PayOSApiResponse.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return PayOSApiResponse.error("fail");
        }
    }

    @GetMapping(path = "/{orderId}")
    public PayOSApiResponse<PaymentLink> getOrderById(@PathVariable("orderId") long orderId) {
        try {
            PaymentLink order = payOS.paymentRequests().get(orderId);
            return PayOSApiResponse.success("ok", order);
        } catch (Exception e) {
            e.printStackTrace();
            return PayOSApiResponse.error(e.getMessage());
        }
    }

    @PutMapping(path = "/{orderId}")
    public PayOSApiResponse<PaymentLink> cancelOrder(@PathVariable("orderId") long orderId) {
        try {
            PaymentLink order = payOS.paymentRequests().cancel(orderId, "change my mind");
            return PayOSApiResponse.success("ok", order);
        } catch (Exception e) {
            e.printStackTrace();
            return PayOSApiResponse.error(e.getMessage());
        }
    }

    @PostMapping(path = "/confirm-webhook")
    public PayOSApiResponse<ConfirmWebhookResponse> confirmWebhook(
            @RequestBody Map<String, String> requestBody) {
        try {
            ConfirmWebhookResponse result = payOS.webhooks().confirm(requestBody.get("webhookUrl"));
            return PayOSApiResponse.success("ok", result);
        } catch (Exception e) {
            e.printStackTrace();
            return PayOSApiResponse.error(e.getMessage());
        }
    }

    @GetMapping(path = "/{orderId}/invoices")
    public PayOSApiResponse<InvoicesInfo> retrieveInvoices(@PathVariable("orderId") long orderId) {
        try {
            InvoicesInfo invoicesInfo = payOS.paymentRequests().invoices().get(orderId);
            return PayOSApiResponse.success("ok", invoicesInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return PayOSApiResponse.error(e.getMessage());
        }
    }

    @GetMapping(path = "/{orderId}/invoices/{invoiceId}/download")
    public ResponseEntity<?> downloadInvoice(
            @PathVariable("orderId") long orderId, @PathVariable("invoiceId") String invoiceId) {
        try {
            FileDownloadResponse invoiceFile =
                    payOS.paymentRequests().invoices().download(invoiceId, orderId);

            if (invoiceFile == null || invoiceFile.getData() == null) {
                return ResponseEntity.status(404).body(PayOSApiResponse.error("invoice not found or empty"));
            }

            ByteArrayResource resource = new ByteArrayResource(invoiceFile.getData());

            HttpHeaders headers = new HttpHeaders();
            String contentType =
                    invoiceFile.getContentType() == null
                            ? MediaType.APPLICATION_PDF_VALUE
                            : invoiceFile.getContentType();
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            headers.set(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + invoiceFile.getFilename() + "\"");
            if (invoiceFile.getSize() != null) {
                headers.setContentLength(invoiceFile.getSize());
            }

            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (APIException e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(PayOSApiResponse.error(e.getErrorDesc().orElse(e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(PayOSApiResponse.error(e.getMessage()));
        }
    }
}