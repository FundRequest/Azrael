package io.fundrequest.azrael.worker.events.listener;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fundrequest.azrael.worker.contracts.ContractEvent;
import io.fundrequest.azrael.worker.contracts.FundRequestContract;
import io.fundrequest.azrael.worker.events.model.ClaimEventDto;
import io.fundrequest.azrael.worker.events.model.FundEventDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import rx.Observable;
import rx.Subscription;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Component
@Slf4j
public class FundRequestEventListener {

    private static final Logger logger = LoggerFactory.getLogger(FundRequestEventListener.class);


    private static final Event FUNDED_EVENT = new Event("Funded",
            Arrays.asList(new TypeReference<Address>() {
            }),
            Arrays.asList(
                    new TypeReference<Bytes32>() {
                    }, new TypeReference<Utf8String>() {
                    }, new TypeReference<Utf8String>() {
                    },
                    new TypeReference<Uint256>() {
                    }));

    private static final Event CLAIMED_EVENT = new Event("Claimed",
            Arrays.asList(new TypeReference<Address>() {
            }),
            Arrays.asList(
                    new TypeReference<Bytes32>() {
                    }, new TypeReference<Utf8String>() {
                    }, new TypeReference<Utf8String>() {
                    },
                    new TypeReference<Uint256>() {
                    }));


    @Autowired
    private Web3j web3j;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${io.fundrequest.contract.address}")
    private String fundrequestContractAddress;
    @Value("${io.fundrequest.azrael.queue.fund}")
    private String fundQueue;
    @Value("${io.fundrequest.azrael.queue.claim}")
    private String claimQueue;
    @Autowired
    private FundRequestContract fundRequestContract;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Subscription liveSubscription;

    @PostConstruct
    public void listenToEvents() {
        subscribeToHistoric();
    }

    @Scheduled(fixedRate = (60000 * 5))
    private void subscribeToLive() {
        if (this.liveSubscription == null) {
            logger.debug("starting live subscription");
            this.liveSubscription = doLiveSubscription();
        } else {
            this.liveSubscription.unsubscribe();
            this.liveSubscription = doLiveSubscription();
        }
    }

    private Subscription doLiveSubscription() {
        return live().subscribe((log) -> {
            try {
                logger.info("Received Live Log!");
                fundRequestContract.getEventParameters(getEvent(log.getTopics()), log)
                        .ifPresent(sendToAzrael(log.getTransactionHash(), log.getBlockHash()));
            } catch (Exception ex) {
                logger.error("unable to get live event parameters", ex);
            }
        });
    }

    private void subscribeToHistoric() {
        logger.debug("starting historic subscription");
        historic().subscribe((log) -> {
            if (log.getLogs() != null && !log.getLogs().isEmpty()) {
                log.getLogs()
                        .forEach((logItem) -> {
                            try {
                                logger.info("Received historic event");
                                final String transactionHash = ((EthLog.LogObject) logItem).getTransactionHash();
                                final String blockhash = ((EthLog.LogObject) logItem).getBlockHash();
                                Event event = getEvent(((EthLog.LogObject) logItem).getTopics());
                                fundRequestContract.getEventParameters(event, (Log) logItem.get())
                                        .filter(this::isValidEvent)
                                        .ifPresent(sendToAzrael(transactionHash, blockhash));
                            } catch (Exception ex) {
                                logger.error("unable to get event parameters", ex);
                            }
                        });
            }
        });
    }

    private Event getEvent(List<String> topics) {
        Event event;
        if (topics.get(0).equals(EventEncoder.encode(FUNDED_EVENT))) {
            event = FUNDED_EVENT;
        } else {
            event = CLAIMED_EVENT;
        }
        return event;
    }

    private boolean isValidEvent(ContractEvent contractEvent) {
        EventValues eventParameters = contractEvent.getEventValues();
        return eventParameters.getNonIndexedValues().size() > 0
                && eventParameters.getIndexedValues().size() > 0;
    }

    private Consumer<ContractEvent> sendToAzrael(final String transactionHash, final String blockHash) {
        return contractEvent -> {
            try {
                EventValues eventValues = contractEvent.getEventValues();
                long timestamp = getTimestamp(blockHash);
                switch (contractEvent.getEventType()) {
                    case FUNDED:
                        sendFundEvent(transactionHash, eventValues, timestamp);
                        break;
                    case CLAIMED:
                        sendClaimEvent(transactionHash, eventValues, timestamp);
                        break;
                    default:
                        logger.debug("Unknown event, not updating");
                }
            } catch (final Exception ex) {
                logger.error("Unable to get event from log", ex);
            }
        };
    }

    private void sendClaimEvent(String transactionHash, EventValues eventValues, long timestamp) throws JsonProcessingException {
        final ClaimEventDto dto = new ClaimEventDto(
                transactionHash,
                eventValues.getIndexedValues().get(0).toString(),
                new String(((byte[]) eventValues.getNonIndexedValues().get(0).getValue()))
                        .chars()
                        .filter(c -> c != 0)
                        .mapToObj(c -> (char) c)
                        .collect(StringBuilder::new,
                                StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString(),
                eventValues.getNonIndexedValues().get(1).getValue().toString(),
                eventValues.getNonIndexedValues().get(2).getValue().toString(),
                eventValues.getNonIndexedValues().get(3).getValue().toString(),
                timestamp
        );
        rabbitTemplate.convertAndSend(claimQueue, objectMapper.writeValueAsString(dto));
    }

    private void sendFundEvent(String transactionHash, EventValues eventValues, long timestamp) throws JsonProcessingException {
        final FundEventDto fundEventDto = new FundEventDto(
                transactionHash,
                eventValues.getIndexedValues().get(0).toString(),
                new String(((byte[]) eventValues.getNonIndexedValues().get(0).getValue()))
                        .chars()
                        .filter(c -> c != 0)
                        .mapToObj(c -> (char) c)
                        .collect(StringBuilder::new,
                                StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString(),
                eventValues.getNonIndexedValues().get(1).getValue().toString(),
                eventValues.getNonIndexedValues().get(2).getValue().toString(),
                timestamp
        );
        rabbitTemplate.convertAndSend(fundQueue, objectMapper.writeValueAsString(fundEventDto));
    }

    private long getTimestamp(final String blockHash) throws java.io.IOException {
        final EthBlock send = web3j.ethGetBlockByHash(blockHash, false).send();
        if (send.getBlock() != null) {
            return send.getBlock().getTimestamp().longValue() * 1000;
        } else {
            return 0;
        }
    }

    private EthFilter contractEventsFilter() {
        EthFilter ethFilter = new EthFilter(DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST, fundrequestContractAddress);
        ethFilter.addOptionalTopics(EventEncoder.encode(FUNDED_EVENT), EventEncoder.encode(CLAIMED_EVENT));
        return ethFilter;
    }

    private Observable<EthLog> historic() {
        return web3j.ethGetLogs(contractEventsFilter()).observable();
    }

    private Observable<Log> live() {
        return web3j.ethLogObservable(contractEventsFilter());
    }
}
