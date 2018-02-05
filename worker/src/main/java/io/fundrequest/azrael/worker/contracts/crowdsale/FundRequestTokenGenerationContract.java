package io.fundrequest.azrael.worker.contracts.crowdsale;

import io.fundrequest.azrael.worker.contracts.crowdsale.event.PaidEvent;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class FundRequestTokenGenerationContract extends Contract {


    public FundRequestTokenGenerationContract(final String contractBinary, final String contractAddress, final Web3j web3j, final Credentials credentails, final BigInteger gasPrice, final BigInteger gasLimit) {
        super(contractBinary, contractAddress, web3j, credentails, gasPrice, gasLimit);
    }


    public Bool personalCapActive() throws ExecutionException, InterruptedException {
        final Function function = new Function("personalCapActive",
                Arrays.asList(),
                Collections.singletonList(new TypeReference<Bool>() {})
        );
        try {
            return executeCallSingleValueReturn(function);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<PaidEvent> getEventParameters(
            Event event, Log log) {
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
        return Optional.of(new PaidEvent(new EventValues(indexedValues, nonIndexedValues)));
    }
}
