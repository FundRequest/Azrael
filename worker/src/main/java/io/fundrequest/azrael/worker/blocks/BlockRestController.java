package io.fundrequest.azrael.worker.blocks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.Web3j;

@RestController
@RequestMapping(value = "/rest/blocks")
public class BlockRestController {

    @Autowired
    private Web3j web3j;

    @RequestMapping(method = RequestMethod.GET)
    public String lastBlock() {
        return web3j.ethBlockNumber().observable().toBlocking().first().getBlockNumber().toString();
    }

}
