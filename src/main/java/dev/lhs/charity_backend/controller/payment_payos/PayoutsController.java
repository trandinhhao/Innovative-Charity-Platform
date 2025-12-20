package dev.lhs.charity_backend.controller.payment_payos;

import dev.lhs.charity_backend.dto.payment_payos.PayOSApiResponse;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.core.Page;
import vn.payos.model.v1.payouts.GetPayoutListParams;
import vn.payos.model.v1.payouts.Payout;
import vn.payos.model.v1.payouts.PayoutApprovalState;
import vn.payos.model.v1.payouts.PayoutRequests;
import vn.payos.model.v1.payouts.batch.PayoutBatchItem;
import vn.payos.model.v1.payouts.batch.PayoutBatchRequest;
import vn.payos.model.v1.payouts.GetPayoutListParams.GetPayoutListParamsBuilder;
import vn.payos.model.v1.payoutsAccount.PayoutAccountInfo;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/payouts")
public class PayoutsController {
    private final PayOS payOS;

    public PayoutsController(PayOS payOSPayout) {
        super();
        this.payOS = payOSPayout;
    }

    @PostMapping("/create")
    public PayOSApiResponse<Payout> create(@RequestBody PayoutRequests body) {
        try {
            if (body.getReferenceId() == null || body.getReferenceId().isEmpty()) {
                body.setReferenceId("payout_" + (System.currentTimeMillis() / 1000));
            }

            Payout payout = payOS.payouts().create(body);
            return PayOSApiResponse.success(payout);

        } catch (Exception e) {
            e.printStackTrace();
            return PayOSApiResponse.error("fail");
        }
    }

    @PostMapping("/batch/create")
    public PayOSApiResponse<Payout> createBatch(@RequestBody PayoutBatchRequest body) {
        try {
            if (body.getReferenceId() == null || body.getReferenceId().isEmpty()) {
                body.setReferenceId("payout_" + (System.currentTimeMillis() / 1000));
            }

            List<PayoutBatchItem> payoutsList = body.getPayouts();
            if (payoutsList == null) {
                return PayOSApiResponse.error("fail");
            }
            for (int i = 0; i < payoutsList.size(); i++) {
                PayoutBatchItem batchItem = payoutsList.get(i);
                if (batchItem.getReferenceId() == null) {
                    batchItem.setReferenceId("payout_" + (System.currentTimeMillis() / 1000) + "_" + i);
                }
            }

            Payout payout = payOS.payouts().batch().create(body);
            return PayOSApiResponse.success(payout);

        } catch (Exception e) {
            e.printStackTrace();
            return PayOSApiResponse.error("fail");
        }
    }

    @GetMapping("/{payoutId}")
    public PayOSApiResponse<Payout> retrieve(@PathVariable String payoutId) {
        try {
            Payout payout = payOS.payouts().get(payoutId);
            return PayOSApiResponse.success(payout);

        } catch (Exception e) {
            e.printStackTrace();
            return PayOSApiResponse.error("fail");
        }
    }

    @GetMapping("/list")
    public PayOSApiResponse<List<Payout>> retrieveList(
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String approvalState,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        try {
            GetPayoutListParamsBuilder paramsBuilder =
                    GetPayoutListParams.builder()
                            .referenceId(referenceId)
                            .category(category)
                            .limit(limit)
                            .offset(offset);
            if (fromDate != null && !fromDate.isEmpty()) {
                paramsBuilder.fromDate(fromDate);
            }
            if (toDate != null && !toDate.isEmpty()) {
                paramsBuilder.toDate(toDate);
            }

            PayoutApprovalState parsedApprovalState = null;
            if (approvalState != null && !approvalState.isEmpty()) {
                try {
                    parsedApprovalState = PayoutApprovalState.valueOf(approvalState.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return PayOSApiResponse.error("Invalid approval state: " + approvalState);
                }
                paramsBuilder.approvalState(parsedApprovalState);
            }

            GetPayoutListParams params = paramsBuilder.build();

            List<Payout> data = new ArrayList<>();
            Page<Payout> page = payOS.payouts().list(params);
            page.autoPager().stream().forEach(data::add);
            return PayOSApiResponse.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return PayOSApiResponse.error("fail");
        }
    }

    @GetMapping("/balance")
    public PayOSApiResponse<PayoutAccountInfo> getAccountBalance() {
        try {
            PayoutAccountInfo accountInfo = payOS.payoutsAccount().balance();
            return PayOSApiResponse.success(accountInfo);

        } catch (Exception e) {
            e.printStackTrace();
            return PayOSApiResponse.error("fail");
        }
    }
}