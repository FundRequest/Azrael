package io.fundrequest.azrael.worker.contracts.platform;


import io.fundrequest.azrael.worker.contracts.platform.event.TokenEvent;
import io.fundrequest.azrael.worker.contracts.platform.event.TokenEventType;
import org.apache.commons.lang3.StringUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FundRequestToken extends Contract {

    public FundRequestToken(final String contractAddress, final Web3j web3j, final Credentials credentials, final BigInteger gasPrice, final BigInteger gasLimit) {
        super("", contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public Optional<TokenEvent> getEventParameters(
            Event event, Log log) {
        final Optional<TokenEventType> eventType = getEventType(event);
        if (!eventType.isPresent()) {
            return Optional.empty();
        }
        final List<String> topics = log.getTopics();
        final String encodedEventSignature = EventEncoder.encode(event);
        if (!topics.get(0).equals(encodedEventSignature)) {
            return Optional.empty();
        }

        final List<Type> indexedValues = new ArrayList<>();
        final List<Type> nonIndexedValues = FunctionReturnDecoder.decode(
                log.getData(), event.getNonIndexedParameters());

        final List<TypeReference<Type>> indexedParameters = event.getIndexedParameters();
        for (int i = 0; i < indexedParameters.size(); i++) {
            Type value = FunctionReturnDecoder.decodeIndexedValue(
                    topics.get(i + 1), indexedParameters.get(i));
            indexedValues.add(value);
        }
        return Optional.of(new TokenEvent(eventType.get(), new EventValues(indexedValues, nonIndexedValues)));
    }


    private Optional<TokenEventType> getEventType(Event event) {
        if (StringUtils.isNotBlank(event.getName())) {
            try {
                return Optional.of(TokenEventType.valueOf(event.getName().toUpperCase()));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}
