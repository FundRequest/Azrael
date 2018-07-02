package io.fundrequest.azrael.worker.events.listener;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fundrequest.azrael.worker.contracts.platform.FundRequestContract;
import io.fundrequest.azrael.worker.contracts.platform.event.PlatformEvent;
import io.fundrequest.azrael.worker.events.model.ClaimEventDto;
import io.fundrequest.azrael.worker.events.model.FundEventDto;
import io.fundrequest.azrael.worker.events.model.RefundEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.datatypes.Event;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import rx.Observable;
import rx.Subscription;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Consumer;

import static io.fundrequest.azrael.worker.contracts.platform.FundRequestContract.CLAIMED_EVENT;
import static io.fundrequest.azrael.worker.contracts.platform.FundRequestContract.FUNDED_EVENT;
import static io.fundrequest.azrael.worker.contracts.platform.FundRequestContract.REFUND_EVENT;

@Component
@ConditionalOnBean(FundRequestContract.class)
public class FundRequestPlatformEventListener {

    private static final Logger logger = LoggerFactory.getLogger(FundRequestPlatformEventListener.class);


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
    @Value("${io.fundrequest.azrael.queue.refund}")
    private String refundQueue;
    @Autowired
    private FundRequestContract fundRequestContract;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Subscription liveSubscription;

    @PostConstruct
    public void importHistoric() {
        subscribeToHistoric();
    }

    @Scheduled(fixedRate = (60000 * 5))
    private void subscribeToLive() {
        final Subscription newLiveSubscription = doLiveSubscription();

        if (this.liveSubscription == null) {
            logger.debug("starting live subscription for platform events");
            this.liveSubscription = newLiveSubscription;
        } else {
            this.liveSubscription.unsubscribe();
            this.liveSubscription = newLiveSubscription;
        }
    }

    private Subscription doLiveSubscription() {
        return live().subscribe((log) -> {
            try {
                logger.debug("Received Live Log!");
                fundRequestContract.getEventParameters(getEvent(log.getTopics()), log)
                                   .ifPresent(sendToAzrael(log));
            } catch (Exception ex) {
                logger.error("unable to get live event parameters", ex);
            }
        });
    }

    private void subscribeToHistoric() {
        logger.debug("starting historic subscription for platform events");
        historic().subscribe((log) -> {
            if (log.getLogs() != null && !log.getLogs().isEmpty()) {
                log.getLogs()
                   .forEach((logItem) -> {
                       try {
                           logger.debug("Received historic event");
                           final Event event = getEvent(((EthLog.LogObject) logItem).getTopics());
                           Log logz = (Log) logItem.get();
                           fundRequestContract.getEventParameters(event, logz)
                                              .filter(this::isValidEvent)
                                              .ifPresent(sendToAzrael(logz));
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
        } else if (topics.get(0).equals(EventEncoder.encode(REFUND_EVENT))) {
            event = REFUND_EVENT;
        } else {
            event = CLAIMED_EVENT;
        }
        return event;
    }

    private boolean isValidEvent(PlatformEvent platformEvent) {
        EventValues eventParameters = platformEvent.getEventValues();
        return eventParameters.getNonIndexedValues().size() > 0
               && eventParameters.getIndexedValues().size() > 0;
    }

    private Consumer<PlatformEvent> sendToAzrael(Log log) {
        return platformEvent -> {
            try {
                EventValues eventValues = platformEvent.getEventValues();
                long timestamp = getTimestamp(log.getBlockHash());
                switch (platformEvent.getEventType()) {
                    case FUNDED:
                        sendFundEvent(log.getTransactionHash(), log.getLogIndexRaw(), eventValues, timestamp);
                        break;
                    case CLAIMED:
                        sendClaimEvent(log.getTransactionHash(), log.getLogIndexRaw(), eventValues, timestamp);
                        break;
                    case REFUND:
                        sendRefundEvent(log.getTransactionHash(), log.getLogIndexRaw(), eventValues, timestamp);
                        break;
                    default:
                        logger.debug("Unknown event, not updating");
                }
            } catch (final Exception ex) {
                logger.error("Unable to get event from log", ex);
            }
        };
    }

    private void sendRefundEvent(String transactionHash, String logIndexRaw, EventValues eventValues, long timestamp) throws JsonProcessingException {
        final RefundEventDto dto = new RefundEventDto(transactionHash,
                                                      logIndexRaw,
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
        rabbitTemplate.convertAndSend(refundQueue, objectMapper.writeValueAsString(dto));
    }

    private void sendClaimEvent(String transactionHash, String logIndex, EventValues eventValues, long timestamp) throws JsonProcessingException {
        final ClaimEventDto dto = new ClaimEventDto(
                transactionHash,
                logIndex,
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
                eventValues.getNonIndexedValues().get(4).getValue().toString(),
                timestamp
        );
        rabbitTemplate.convertAndSend(claimQueue, objectMapper.writeValueAsString(dto));
    }

    private void sendFundEvent(String transactionHash, String logIndex, EventValues eventValues, long timestamp) throws JsonProcessingException {
        final FundEventDto fundEventDto = new FundEventDto(
                transactionHash,
                logIndex,
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
        ethFilter.addOptionalTopics(EventEncoder.encode(FUNDED_EVENT), EventEncoder.encode(CLAIMED_EVENT), EventEncoder.encode(REFUND_EVENT));
        return ethFilter;
    }

    private Observable<EthLog> historic() {
        return web3j.ethGetLogs(contractEventsFilter()).observable();
    }

    private Observable<Log> live() {
        return web3j.ethLogObservable(contractEventsFilter());
    }
}
