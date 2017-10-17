package io.fundrequest.azrael.worker.contracts.rest;

import io.fundrequest.azrael.worker.contracts.FundRequestContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RequestMapping(value = "/rest")
@RestController
public class FundRequestContractRestService {

    @Autowired
    private FundRequestContract fundRequestContract;

    @RequestMapping(value = "/balance", method = POST)
    public long getBalance(@RequestBody final String data) {
        //TODO: remove stub and call fundrequestcontract with data -> byte[]
        if (data != null && data.equals("Davy")) {
            return 100;
        } else {
            return 0;
        }
    }
}
