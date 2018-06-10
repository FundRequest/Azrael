package io.fundrequest.azrael.worker.contracts.refund.controller;


import io.fundrequest.azrael.worker.contracts.refund.sign.RefundSignature;
import io.fundrequest.azrael.worker.contracts.refund.sign.RefundSigningService;
import io.fundrequest.azrael.worker.contracts.refund.sign.SignRefundCommand;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RequestMapping(value = "/rest")
@RestController
public class ClaimController {

    private RefundSigningService refundSigningService;

    public ClaimController(final RefundSigningService refundSigningService) {
        this.refundSigningService = refundSigningService;
    }

    @RequestMapping(value = "/refund/sign", method = POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public RefundSignature getSignature(@RequestBody @Valid SignRefundCommand command, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException("not a valid request");
        }
        return refundSigningService.signClaim(command);
    }
}
