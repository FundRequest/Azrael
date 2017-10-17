package io.fundrequest.azrael.worker.contracts.rest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RequestMapping(value = "/rest")
@RestController
public class FundRequestContractRestService {

    @RequestMapping(value = "/balance", method = POST)
    public long getBalance(@RequestBody final String data) {
        //returning stub data atm
        if (data != null && data.equals("Davy")) {
            return 100;
        } else {
            return 0;
        }
    }
}
