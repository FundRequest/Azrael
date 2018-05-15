package io.fundrequest.azrael.worker.contracts.claim.controller;


import io.fundrequest.azrael.worker.contracts.claim.ClaimTransaction;
import io.fundrequest.azrael.worker.contracts.claim.sign.ClaimService;
import io.fundrequest.azrael.worker.contracts.claim.sign.ClaimSignature;
import io.fundrequest.azrael.worker.contracts.claim.sign.ClaimSigningService;
import io.fundrequest.azrael.worker.contracts.claim.sign.SignClaimCommand;
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

    private ClaimSigningService claimSigningService;
    private ClaimService claimService;

    public ClaimController(ClaimSigningService claimSigningService, ClaimService claimService) {
        this.claimSigningService = claimSigningService;
        this.claimService = claimService;
    }

    @RequestMapping(value = "/claims/sign", method = POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ClaimSignature getSignature(@RequestBody @Valid SignClaimCommand command, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException("not a valid request");
        }
        return claimSigningService.signClaim(command);
    }

    @RequestMapping(value = "/claims/submit", method = POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ClaimTransaction approve(@RequestBody @Valid ClaimSignature command, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException("Invalid signature");
        } else {
            return ClaimTransaction.builder()
                                   .transactionHash(claimService.receiveApprovedClaim(command))
                                   .build();
        }
    }
}
