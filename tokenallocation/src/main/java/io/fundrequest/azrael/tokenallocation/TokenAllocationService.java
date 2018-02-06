package io.fundrequest.azrael.tokenallocation;

import io.fundrequest.azrael.tokenallocation.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import java.math.BigInteger;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@Service
@Slf4j
public class TokenAllocationService {

    @Value("${io.fundrequest.tge.address}")
    private String tgeAddress;

    @Autowired
    private Web3j web3j;
    @Autowired
    private SecurityConfig securityConfig;

    final BigInteger gasLimit = BigInteger.valueOf(100000);
    final BigInteger gasPrice = BigInteger.valueOf(3000000000L); //3 gwei

    public void allocate(final String beneficiary, final BigInteger tokensSold) {
        try {
            final Credentials credentials = securityConfig.getKey();
            final BigInteger nonce = web3j.ethGetTransactionCount(prettify(credentials.getAddress()), DefaultBlockParameterName.LATEST).send().getTransactionCount();

            final Function function = new Function(
                    "allocateTokens",
                    asList(new org.web3j.abi.datatypes.Address(beneficiary),
                            new org.web3j.abi.datatypes.generated.Uint256(tokensSold)),
                    emptyList());

            final String encodedFunction = FunctionEncoder.encode(function);

            final RawTransaction transaction = RawTransaction.createTransaction(nonce, gasPrice,
                    gasLimit,
                    tgeAddress, encodedFunction);

            final byte[] signedMessage = sign(credentials.getEcKeyPair(), transaction);
            final String signedMessageAsHex = prettify(Hex.toHexString(signedMessage));
            final EthSendTransaction send = web3j.ethSendRawTransaction(signedMessageAsHex).send();


            if (send.getError() != null) {
                log.error(send.getError().getMessage());
                throw new IllegalArgumentException(send.getError().getMessage());
            } else {
                log.info(send.getTransactionHash());
            }
        } catch (final Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    private byte[] sign(final ECKeyPair keyPair, final RawTransaction etherTransaction) {
        return TransactionEncoder.signMessage(etherTransaction, Credentials.create(keyPair));
    }

    private String prettify(final String address) {
        if (!address.startsWith("0x")) {
            return String.format("0x%s", address);
        } else {
            return address;
        }
    }
}
