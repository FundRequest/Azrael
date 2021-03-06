package io.fundrequest.azrael.worker.events.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fundrequest.azrael.worker.contracts.crowdsale.FundRequestTokenGenerationContract;
import io.fundrequest.azrael.worker.contracts.crowdsale.event.PaidEvent;
import io.fundrequest.azrael.worker.events.model.PaidEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Log;
import rx.Observable;
import rx.Subscription;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

@Component
@Slf4j
@ConditionalOnBean(FundRequestTokenGenerationContract.class)
public class FundRequestTGEEventListener {


    private static final Event PAID_EVENT = new Event("Paid",
                                                      Arrays.asList(new TypeReference<Address>(true) {
                                                                    },
                                                                    new TypeReference<Uint256>() {
                                                                    }, new TypeReference<Uint256>() {
                                                              }, new TypeReference<Bool>() {
                                                              }));

    @Autowired
    private FundRequestTokenGenerationContract tokenGenerationContract;
    @Value("${io.fundrequest.tge.address}")
    private String tokenGenerationContractAddress;
    @Autowired
    private Web3j web3j;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${io.fundrequest.azrael.queue.paid}")
    private String paidQueue;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Subscription subscription;

//    @Scheduled(fixedRate = (60000 * 5))
    private void subscribeToLive() {
        if (this.subscription == null) {
            log.debug("starting paid subscription");
            this.subscription = doSubscription();
        } else {
            final Subscription newSubscription = doSubscription();
            this.subscription.unsubscribe();
            this.subscription = newSubscription;
        }
    }

    private Subscription doSubscription() {
        return live().subscribe((logz) -> {
            try {
                tokenGenerationContract.getEventParameters(PAID_EVENT, logz)
                                       .ifPresent(sendToAzrael(logz));
            } catch (Exception ex) {
                log.error("unable to get live event parameters", ex);
            }
        });
    }

    private long getTimestamp(final String blockHash) throws java.io.IOException {
        final EthBlock send = web3j.ethGetBlockByHash(blockHash, false).send();
        if (send.getBlock() != null) {
            return send.getBlock().getTimestamp().longValue() * 1000;
        } else {
            return 0;
        }
    }


    private Consumer<PaidEvent> sendToAzrael(Log logz) {
        return platformEvent -> {
            try {
                final EventValues eventValues = platformEvent.getEventValues();
                final long timestamp = getTimestamp(logz.getBlockHash());
                sendPaidEvent(logz.getTransactionHash(), logz.getLogIndexRaw(), eventValues, timestamp);
            } catch (final Exception ex) {
                log.error("Unable to get event from log", ex);
            }
        };
    }

    private void sendPaidEvent(String transactionHash,
                               String logIndex,
                               EventValues eventValues,
                               long timestamp) throws JsonProcessingException {
        final PaidEventDto paidEvent = new PaidEventDto(
                transactionHash,
                logIndex,
                eventValues.getIndexedValues().get(0).toString(),
                eventValues.getNonIndexedValues().get(0).getValue().toString(),
                eventValues.getNonIndexedValues().get(1).getValue().toString(),
                timestamp,
                (boolean) eventValues.getNonIndexedValues().get(2).getValue()
        );
        rabbitTemplate.convertAndSend(paidQueue, objectMapper.writeValueAsString(paidEvent));
    }

    private EthFilter contractEventsFilter(final Optional<DefaultBlockParameter> from,
                                           final Optional<DefaultBlockParameter> to) {
        EthFilter ethFilter = new EthFilter(from.orElse(DefaultBlockParameterName.EARLIEST),
                                            to.orElse(DefaultBlockParameterName.LATEST), tokenGenerationContractAddress);
        ethFilter.addOptionalTopics(EventEncoder.encode(PAID_EVENT));
        return ethFilter;
    }

    private Observable<Log> live() {
        return web3j.ethLogObservable(contractEventsFilter(Optional.of(DefaultBlockParameterName.LATEST), Optional.empty()));
    }
}
