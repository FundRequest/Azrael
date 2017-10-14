package io.fundrequest.azrael.worker.events;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.fundrequest.azrael.worker.contracts.FundRequestContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes20;
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
import java.util.List;

@Component
public class FundRequestEventListener {

    public static final List<TypeReference<?>> arguments = Arrays.asList(
            new TypeReference<Bytes20>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Bytes32>() {
            }
    );

    private static final Logger logger = LoggerFactory.getLogger(FundRequestEventListener.class);

    public static final Event FUNDED_EVENT = new Event("Funded",
            Arrays.asList(new TypeReference<Address>() {
            }),
            Arrays.asList(new TypeReference<Uint256>() {
            }, new TypeReference<Bytes32>() {
            }));
    @Autowired
    private Web3j web3j;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${io.fundrequest.contract.address}")
    private String fundrequestContractAddress;

    @PostConstruct
    public void listenToEvents() {
        events().subscribe((log) -> {
            if (log.getLogs() != null && !log.getLogs().isEmpty()) {
                log.getLogs()
                        .forEach((logItem) -> {
                            try {
                                final String logsAsString = objectMapper.writeValueAsString(logItem);
                                //     rabbitTemplate.convertAndSend("azrael_rinkeby", logsAsString);
                                TypeReference<Address> typeReference = new TypeReference<Address>() {
                                };
                                Type type = FunctionReturnDecoder.decodeIndexedValue(((EthLog.LogObject) logItem).getData(), typeReference);
                                FunctionReturnDecoder.decode(((EthLog.LogObject) logItem).getData(), FUNDED_EVENT.getNonIndexedParameters());
                                FundRequestContract contract = new FundRequestContract(fundrequestContractAddress, web3j, Credentials.create(ECKeyPair.create(BigInteger.ZERO)), BigInteger.TEN, BigInteger.ONE);
                                EventValues eventParameters = contract.getEventParameters(FUNDED_EVENT, (Log) logItem.get());
                                if (isValidEvent(eventParameters)) {
                                    String request = getRequest(eventParameters);
                                    logger.info("Funded: {}", request);
                                }
                                System.out.println("lol");
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

    private String getRequest(EventValues eventParameters) {
        return new String(((byte[]) eventParameters.getNonIndexedValues().get(1).getValue()))
                                                .chars()
                                                .filter(c -> c != 0)
                                                .mapToObj(c -> (char) c)
                                                .collect(StringBuilder::new,
                                                        StringBuilder::appendCodePoint, StringBuilder::append)
                                                .toString();
    }

    private boolean isValidEvent(EventValues eventParameters) {
        return eventParameters.getNonIndexedValues().size() == 2
                && eventParameters.getIndexedValues().size() == 1;
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
