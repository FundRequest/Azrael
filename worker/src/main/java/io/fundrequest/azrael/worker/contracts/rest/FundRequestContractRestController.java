package io.fundrequest.azrael.worker.contracts.rest;

import io.fundrequest.azrael.worker.contracts.FundRequestContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RequestMapping(value = "/rest")
@RestController
public class FundRequestContractRestController {

    @Autowired
    private FundRequestContract fundRequestContract;

    @RequestMapping(value = "/balance", method = POST)
    public String getBalance(@RequestBody final String data) {
        if (data.getBytes().length > 32) {
            throw new IllegalArgumentException("data can only be max 32 bytes");
        }
        try {
            return fundRequestContract.getBalance(data);
        } catch (Exception ex) {
            throw new IllegalArgumentException("error while trying to call conract", ex);
        }
    }
}
