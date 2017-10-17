package io.fundrequest.azrael.worker.events.listener;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.fundrequest.azrael.worker.contracts.FundRequestContract;
import io.fundrequest.azrael.worker.events.model.FundedEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import rx.Observable;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.Consumer;

@Component
@Slf4j
public class FundRequestEventListener {

    private static final Logger logger = LoggerFactory.getLogger(FundRequestEventListener.class);

    private FundRequestContract fundRequestContract;


    private static final Event FUNDED_EVENT = new Event("Funded",
            Arrays.asList(new TypeReference<Address>() {
            }),
            Arrays.asList(new TypeReference<Uint256>() {
            }, new TypeReference<Bytes32>() {
            }, new TypeReference<Utf8String>() {
            }));


    @Autowired
    private Web3j web3j;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${io.fundrequest.contract.address}")
    private String fundrequestContractAddress;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void listenToEvents() {

        fundRequestContract = new FundRequestContract(fundrequestContractAddress, web3j, Credentials.create(ECKeyPair.create(BigInteger.ZERO)), BigInteger.TEN, BigInteger.ONE);

        events().subscribe((log) -> {
            if (log.getLogs() != null && !log.getLogs().isEmpty()) {
                log.getLogs()
                        .forEach((logItem) -> {
                            try {
                                String transactionHash = ((EthLog.LogObject) logItem).getTransactionHash();
                                fundRequestContract.getEventParameters(FUNDED_EVENT, (Log) logItem.get())
                                        .filter(this::isValidEvent)
                                        .ifPresent(sendToAzrael(transactionHash));
                            } catch (Exception ex) {
                                logger.error("unable to get event parameters", ex);
                            }
                        });
            }
        });

        live().subscribe((log) -> {
            try {
                fundRequestContract.getEventParameters(FUNDED_EVENT, log)
                        .ifPresent(sendToAzrael(log.getTransactionHash()));
            } catch (Exception ex) {
                logger.error("unable to get live event parameters", ex);
            }
        });
    }

    private boolean isValidEvent(EventValues eventParameters) {
        return eventParameters.getNonIndexedValues().size() == 3
                && eventParameters.getIndexedValues().size() == 1;
    }

    private Consumer<EventValues> sendToAzrael(String transactionHash) {
        return eventValues -> {
            try {
                final FundedEvent fundedEvent = new FundedEvent(
                        transactionHash,
                        eventValues.getIndexedValues().get(0).getValue().toString(),
                        eventValues.getNonIndexedValues().get(0).getValue().toString(),
                        new String(((byte[]) eventValues.getNonIndexedValues().get(1).getValue()))
                                .chars()
                                .filter(c -> c != 0)
                                .mapToObj(c -> (char) c)
                                .collect(StringBuilder::new,
                                        StringBuilder::appendCodePoint, StringBuilder::append)
                                .toString(),
                        eventValues.getNonIndexedValues().get(2).getValue().toString()
                );

                rabbitTemplate.convertAndSend("azrael_rinkeby", objectMapper.writeValueAsString(fundedEvent));
            } catch (final Exception ex) {
                logger.error("Unable to get event from log", ex);
            }
        };
    }

    private EthFilter fundedEventFilter() {
        EthFilter ethFilter = new EthFilter(DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST, fundrequestContractAddress);
        String encodedEventSignature = EventEncoder.encode(FUNDED_EVENT);
        ethFilter.addSingleTopic(encodedEventSignature);
        return ethFilter;
    }

    private Observable<EthLog> events() {
        return web3j.ethGetLogs(fundedEventFilter()).observable();
    }

    private Observable<Log> live() {
        return web3j.ethLogObservable(fundedEventFilter());
    }
}
