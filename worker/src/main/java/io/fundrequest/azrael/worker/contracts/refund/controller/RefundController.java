package io.fundrequest.azrael.worker.contracts.refund.controller;


import io.fundrequest.azrael.worker.contracts.refund.RefundTransaction;
import io.fundrequest.azrael.worker.contracts.refund.service.RefundService;
import io.fundrequest.azrael.worker.contracts.refund.sign.RefundRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.util.StringUtils.isEmpty;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RequestMapping(value = "/rest")
@RestController
public class RefundController {

    private RefundService refundService;

    public RefundController(final RefundService refundService) {
        this.refundService = refundService;
    }

    @RequestMapping(value = "/refund/submit", method = POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public RefundTransaction submit(@RequestBody @Valid RefundRequest command, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException("Invalid signature");
        } else {
            final String transactionHash = refundService.submit(command);
            if (isEmpty(transactionHash)) {
                throw new IllegalArgumentException("Problem occurred when trying to send transaction");
            }
            return RefundTransaction.builder()
                                    .transactionHash(transactionHash)
                                    .build();
        }
    }
}
