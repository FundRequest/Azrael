package io.fundrequest.azrael.worker.events;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import rx.Observable;

import javax.annotation.PostConstruct;

@Component
public class FundRequestEventListener {

    final EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST, "0x87d697be055a01984af5b9c96129420b3fba951c");

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Web3j web3j;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void listenToEvents() {
        events().subscribe((log) -> {
            if (log.getLogs() != null && !log.getLogs().isEmpty()) {
                log.getLogs()
                        .forEach((logItem) -> {
                            try {
                                final String logsAsString = objectMapper.writeValueAsString(logItem);
                           //     rabbitTemplate.convertAndSend("azrael_rinkeby", logsAsString);
                                System.out.println(logsAsString);
                            } catch (Exception ex) {
                                System.out.println(ex);
                            }
                        });
            }
        });

        live().subscribe((log) -> {
            try {
                final String logsAsString = objectMapper.writeValueAsString(log);
             //   rabbitTemplate.convertAndSend("azrael_rinkeby", logsAsString);
                System.out.println(logsAsString);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        });
    }

    private Observable<EthLog> events() {
        return web3j.ethGetLogs(filter).observable();
    }

    private Observable<Log> live() {
        return web3j.ethLogObservable(filter);
    }

    /*
       private List<EthLog.LogResult> createFilterForEvent(
            String encodedEventSignature, String contractAddress) throws Exception {
        EthFilter ethFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                contractAddress
        );

        ethFilter.addSingleTopic(encodedEventSignature);

        EthLog ethLog = parity.ethGetLogs(ethFilter).send();
        return ethLog.getLogs();
    }


            // check function signature - we only have a single topic our event signature,
        // there are no indexed parameters in this example
        String encodedEventSignature = EventEncoder.encode(event);
        assertThat(topics.get(0),
                is(encodedEventSignature));

        // verify our two event parameters
        List<Type> results = FunctionReturnDecoder.decode(
                log.getData(), event.getNonIndexedParameters());
        assertThat(results, equalTo(Arrays.asList(
                new Uint256(BigInteger.valueOf(7)), new Uint256(BigInteger.valueOf(13)))));
     */
}
