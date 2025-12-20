package dev.lhs.charity_backend.dto.payment_payos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class CreatePaymentLinkRequestBody {
    private String productName;
    private String description;
    private String returnUrl;
    private int price;
    private String cancelUrl;

}