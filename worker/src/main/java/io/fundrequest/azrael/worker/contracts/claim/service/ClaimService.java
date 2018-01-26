package io.fundrequest.azrael.worker.contracts.claim.service;

import io.fundrequest.azrael.worker.contracts.platform.FundRequestContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClaimService {

    @Autowired
    private FundRequestContract fundRequestContract;

    public void submitClaim() {

    }

}
