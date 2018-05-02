package io.fundrequest.azrael.worker.contracts.claim.sign;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import static io.fundrequest.azrael.worker.utils.AddressUtils.prettify;
import static java.util.Collections.emptyList;

@Component
@Slf4j
public class ClaimService {

    private final ECKeyPair keyPair;
    private Web3j web3j;
    private String fundrequestContractAddress;
    private ObjectMapper objectMapper;
    private String gasPrice;
    private String gasLimit;

    public ClaimService(@Value("${io.fundrequest.execute.account}") String executingAccount, Web3j web3j, @Value("${io.fundrequest.contract.address}") String fundrequestContractAddress, ObjectMapper objectMapper, @Value("${io.fundrequest.azrael.claim.gasprice}") String gasPrice, @Value("${io.fundrequest.azrael.claim.gaslimit}") String gasLimit) {
        this.keyPair = getPrivateKey(executingAccount);
        this.web3j = web3j;
        this.fundrequestContractAddress = fundrequestContractAddress;
        this.objectMapper = objectMapper;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
    }

    private ECKeyPair getPrivateKey(String signingAccount) {
        BigInteger key = new BigInteger(signingAccount, 16);
        return ECKeyPair.create(key.toByteArray());
    }

    public void receiveApprovedClaim(final Object sig) {
        ClaimSignature claimSignature = null;
        try {
            claimSignature = objectMapper.readValue((byte[]) sig, ClaimSignature.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        final Function function = toClaimFunction(claimSignature);
        final String encodedFunction = FunctionEncoder.encode(function);
        final RawTransaction transaction = createTransaction(encodedFunction);
        final byte[] signedMessage = sign(transaction);
        final String signedMessageAsHex = prettify(Hex.toHexString(signedMessage));

        final EthSendTransaction send;
        try {
            send = web3j.ethSendRawTransaction(signedMessageAsHex).send();
            log.info("Claim txHash: {}", send.getTransactionHash());
        } catch (IOException e) {
            log.error("Error when claiming", e);
        }

    }

    private RawTransaction createTransaction(String encodedFunction) {
        return RawTransaction.createTransaction(
                calculateNonce().getTransactionCount(),
                new BigInteger(gasPrice),
                new BigInteger(gasLimit),
                fundrequestContractAddress,
                encodedFunction);
    }

    private Function toClaimFunction(ClaimSignature claimSignature) {
        return new Function(
                "claim",
                Arrays.asList(
                        toBytes32(claimSignature.getPlatform()),
                        new org.web3j.abi.datatypes.Utf8String(claimSignature.getPlatformId()),
                        new org.web3j.abi.datatypes.Utf8String(claimSignature.getSolver()),
                        new org.web3j.abi.datatypes.Address(claimSignature.getAddress()),
                        toBytes32(claimSignature.getR()),
                        toBytes32(claimSignature.getS()),
                        new org.web3j.abi.datatypes.generated.Uint8(claimSignature.getV())

                             ),
                emptyList());
    }

    private Bytes32 toBytes32(final String data) {
        return new Bytes32(Arrays.copyOf(data.getBytes(), 32));
    }

    private EthGetTransactionCount calculateNonce() {
        String address = prettify(Keys.getAddress(keyPair));
        return web3j.ethGetTransactionCount(
                prettify(address),
                DefaultBlockParameterName.LATEST
        ).observable().toBlocking().first();
    }

    private byte[] sign(final RawTransaction etherTransaction) {
        return TransactionEncoder.signMessage(etherTransaction, Credentials.create(keyPair));
    }
}
