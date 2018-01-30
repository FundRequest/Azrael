package io.fundrequest.azrael.worker.contracts.platform;

import io.fundrequest.azrael.worker.contracts.claim.sign.ClaimSignature;
import org.apache.commons.lang3.StringUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class FundRequestContract extends Contract {
    public FundRequestContract(final String contractBinary, final String contractAddress, final Web3j web3j, final TransactionManager transactionManager, final BigInteger gasPrice, final BigInteger gasLimit) {
        super(contractBinary, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public FundRequestContract(final String contractBinary, final String contractAddress, final Web3j web3j, final Credentials credentials, final BigInteger gasPrice, final BigInteger gasLimit) {
        super(contractBinary, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public String getBalance(final String data, final String platformId) throws ExecutionException, InterruptedException {
        return getBalance(toContractBytes32(data), toContractBytes32(platformId)).getValue().toString();
    }

    public boolean claim(final ClaimSignature claimSignature) {
        return doClaim(
                toContractBytes32(claimSignature.getPlatform()),
                toContractBytes32(claimSignature.getPlatformId()),
                claimSignature.getSolver(),
                claimSignature.getAddress(),
                toContractBytes32(claimSignature.getR()),
                toContractBytes32(claimSignature.getS()),
                claimSignature.getV()
        );
    }

    private boolean doClaim(byte[] platform, byte[] platformId, final String solver, final String solverAddress, byte[] r, byte[] s, int v) {
        final Function claim = new Function("claim",
                Arrays.asList(
                        new Bytes32(platform),
                        new Bytes32(platformId),
                        new Bytes32(toContractBytes32(solver)),
                        new Address(solverAddress),
                        new Bytes32(r),
                        new Bytes32(s),
                        new Uint8(v)
                ),
                Arrays.asList(new TypeReference<Bool>() {
                }));
        try {
            Bool bool = this.executeCallSingleValueReturn(claim, Bool.class);
            return bool.getValue();
        } catch (final Exception exception) {
            return false;
        }
    }

    private byte[] toContractBytes32(final String data) {
        return Arrays.copyOf(data.getBytes(), 32);
    }

    public Optional<PlatformEvent> getEventParameters(
            Event event, Log log) {
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

    private Uint256 getBalance(byte[] data, byte[] platformId) throws ExecutionException, InterruptedException {
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
