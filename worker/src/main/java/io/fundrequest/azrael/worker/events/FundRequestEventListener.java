package io.fundrequest.azrael.worker.events;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import rx.Observable;

@Component
public class FundRequestEventListener {

    @Autowired
    private Web3j web3j;

    public Observable<EthBlock> getBlocks() {
        final EthFilter filter = new EthFilter(new DefaultBlockParameterNumber(0),
                DefaultBlockParameterName.LATEST, "0xb6a0d43b4dd2024861578ae165ced97ec2d70a16");
        return web3j.blockObservable(false);
    }

}
