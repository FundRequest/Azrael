package io.fundrequest.azrael.tokenallocation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.RawTransaction;

import java.math.BigInteger;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@Service
public class TokenAllocationService {

    @Value("${io.fundrequest.tge.address}")
    private String tgeAddress;

    BigInteger gasLimit = BigInteger.valueOf(52048);
    BigInteger gasPrice = BigInteger.valueOf(0);

    public void allocate() {


        Function function = new Function(
                "transfer",
                asList(new org.web3j.abi.datatypes.Address(to),
                        new org.web3j.abi.datatypes.generated.Uint256(balance)),
                emptyList());


        String encodedFunction = FunctionEncoder.encode(function);

        RawTransaction transaction = RawTransaction.createTransaction(nonce, gasPrice,
                gasLimit,
                tokenAddress, encodedFunction);

    }

}
