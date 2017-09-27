package io.fundrequest.azrael.worker.blocks;

import io.fundrequest.azrael.worker.events.FundRequestEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.web3j.protocol.Web3j;

@RestController
@RequestMapping(value = "/rest/blocks")
public class BlockRestController {

    @Autowired
    private Web3j web3j;
    @Autowired
    private FundRequestEventListener fundRequestEventListener;

    @RequestMapping(value = "/latest", method = RequestMethod.GET)
    public String lastBlock() {
        return web3j.ethBlockNumber().observable().toBlocking().first().getBlockNumber().toString();
    }

    @RequestMapping(value = "/stream", method = RequestMethod.GET)
    public SseEmitter streamBlocks() {
        final SseEmitter emitter = new SseEmitter();
        fundRequestEventListener.getBlocks()
                .subscribe(next -> {
                    try {
                        emitter.send(next.getBlock());
                    } catch (Exception exceptione) {
                        System.out.println(exceptione);
                    }
                });
        return emitter;
    }
}
