package io.fundrequest.azrael.worker.contracts.platform;

import io.fundrequest.azrael.worker.contracts.platform.event.PlatformEvent;
import io.fundrequest.azrael.worker.contracts.platform.event.PlatformEventType;
import org.apache.commons.lang3.StringUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class FundRequestContract extends Contract {

    public static final Event FUNDED_EVENT = new Event("Funded",
                                                       Arrays.asList(
                                                               new TypeReference<Address>(true) {
                                                               }, new TypeReference<Bytes32>() {
                                                               }, new TypeReference<Utf8String>() {
                                                               }, new TypeReference<Address>() {
                                                               },
                                                               new TypeReference<Uint256>() {
                                                               }));

    public static final Event CLAIMED_EVENT = new Event("Claimed",
                                                        Arrays.asList(
                                                                new TypeReference<Address>(true) {
                                                                }, new TypeReference<Bytes32>() {
                                                                }, new TypeReference<Utf8String>() {
                                                                }, new TypeReference<Utf8String>() {
                                                                }, new TypeReference<Address>() {
                                                                },
                                                                new TypeReference<Uint256>() {
                                                                }));

    public static final Event REFUND_EVENT = new Event("Refund",
                                                       Arrays.asList(
                                                               new TypeReference<Address>(true) {
                                                               }, new TypeReference<Bytes32>() {
                                                               }, new TypeReference<Utf8String>() {
                                                               }, new TypeReference<Address>() {
                                                               }, new TypeReference<Uint256>() {
                                                               }));


    public FundRequestContract(final String contractAddress,
                               final Web3j web3j,
                               final Credentials credentials,
                               final BigInteger gasPrice,
                               final BigInteger gasLimit) {
        super("", contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public String getBalance(final String data,
                             final String platformId) {
        return getBalance(toContractBytes32(data), toContractBytes32(platformId)).getValue().toString();
    }

    private byte[] toContractBytes32(final String data) {
        return Arrays.copyOf(data.getBytes(), 32);
    }

    public Optional<PlatformEvent> getEventParameters(
            Event event,
            Log log) {
        final Optional<PlatformEventType> eventType = getEventType(event);
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
        return Optional.of(new PlatformEvent(eventType.get(), new EventValues(indexedValues, nonIndexedValues)));
    }

    private Uint256 getBalance(byte[] data,
                               byte[] platformId) {
        final Function function = new Function("balance",
                                               Arrays.asList(new Bytes32(data), new Bytes32(platformId)),
                                               Collections.singletonList(new TypeReference<Uint256>() {
                                               })
        );
        try {
            return executeCallSingleValueReturn(function);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<PlatformEventType> getEventType(Event event) {
        if (StringUtils.isNotBlank(event.getName())) {
            try {
                return Optional.of(PlatformEventType.valueOf(event.getName().toUpperCase()));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
