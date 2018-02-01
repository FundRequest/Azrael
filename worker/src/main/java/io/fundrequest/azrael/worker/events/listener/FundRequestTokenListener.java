package io.fundrequest.azrael.worker.events.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fundrequest.azrael.worker.contracts.platform.FundRequestToken;
import io.fundrequest.azrael.worker.contracts.platform.TokenEvent;
import io.fundrequest.azrael.worker.events.model.TransferEventDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Log;
import rx.Observable;
import rx.Subscription;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Component
@ConditionalOnProperty(name = "io.fundrequest.token.address")
public class FundRequestTokenListener {

    private static final Logger logger = LoggerFactory.getLogger(FundRequestTokenListener.class);

    private static final Event TRANSFER_EVENT = new Event("Transfer",
            Arrays.asList(
                    new TypeReference<Address>() {
                    },
                    new TypeReference<Address>() {
                    }
            ),
            Arrays.asList(
                    new TypeReference<Uint256>() {
                    })
    );

    private static final Event CLAIMED_TOKENS_EVENT = new Event("ClaimedTokens",
            Arrays.asList(
                    new TypeReference<Address>() {
                    },
                    new TypeReference<Address>() {
                    }
            ),
            Arrays.asList(
                    new TypeReference<Uint256>() {
                    })
    );


    @Autowired
    private Web3j web3j;
    @Value("${io.fundrequest.azrael.queue.transfer}")
    private String transferQueue;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${io.fundrequest.token.address}")
    private String tokenContractAddress;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private Subscription liveSubscription;
    @Autowired
    private FundRequestToken tokenContract;


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
                tokenContract.getEventParameters(getEvent(log.getTopics()), log)
                        .ifPresent(sendToAzrael(log.getTransactionHash(), log.getBlockHash()));
            } catch (Exception ex) {
                logger.error("unable to get live event parameters", ex);
            }
        });
    }

    private Event getEvent(List<String> topics) {
        Event event;
        if (topics.get(0).equals(EventEncoder.encode(TRANSFER_EVENT))) {
            event = TRANSFER_EVENT;
        } else {
            event = CLAIMED_TOKENS_EVENT;
        }
        return event;
    }


    private Consumer<TokenEvent> sendToAzrael(final String transactionHash, final String blockHash) {
        return tokenEvent -> {
            try {
                final EventValues eventValues = tokenEvent.getEventValues();
                final long timestamp = getTimestamp(blockHash);
                switch (tokenEvent.getEventType()) {
                    case TRANSFER:
                        sendTransferEvent(transactionHash, eventValues, timestamp);
                        break;
                    default:
                        logger.debug("Unknown event, not updating");
                }
            } catch (final Exception ex) {
                logger.error("Unable to get event from log", ex);
            }
        };
    }

    private void sendTransferEvent(String transactionHash, EventValues eventValues, long timestamp) throws JsonProcessingException {
        final TransferEventDto transferEventDto = new TransferEventDto(
                transactionHash,
                eventValues.getIndexedValues().get(0).toString(),
                eventValues.getIndexedValues().get(1).getValue().toString(),
                eventValues.getNonIndexedValues().get(0).getValue().toString(),
                timestamp
        );
        rabbitTemplate.convertAndSend(transferQueue, objectMapper.writeValueAsString(transferEventDto));
    }

    private EthFilter contractEventsFilter() {
        EthFilter ethFilter = new EthFilter(DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST, tokenContractAddress);
        ethFilter.addOptionalTopics(EventEncoder.encode(TRANSFER_EVENT));
        return ethFilter;
    }

    private long getTimestamp(final String blockHash) throws java.io.IOException {
        final EthBlock send = web3j.ethGetBlockByHash(blockHash, false).send();
        if (send.getBlock() != null) {
            return send.getBlock().getTimestamp().longValue() * 1000;
        } else {
            return 0;
        }
    }

    private Observable<Log> live() {
        return web3j.ethLogObservable(contractEventsFilter());
    }

}
