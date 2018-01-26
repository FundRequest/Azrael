package io.fundrequest.azrael.worker.config;

import io.fundrequest.azrael.worker.contracts.crowdsale.FundRequestTokenGenerationContract;
import io.fundrequest.azrael.worker.contracts.platform.FundRequestContract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;

import java.math.BigInteger;

@Configuration
public class ContractsConfig {

    @Bean
    public FundRequestContract provideContract(
            @Value("${io.fundrequest.contract.address}") final String address,
            @Value("${io.fundrequest.contract.binary}") final String binary,
            final Web3j web3j) {
        return new FundRequestContract(
                binary, address, web3j, Credentials.create(ECKeyPair.create(BigInteger.ONE)), BigInteger.ONE, BigInteger.ONE);
    }

    @Bean
    public FundRequestTokenGenerationContract provideTokenGenerationContract(
            @Value("${io.fundrequest.tge.address}") final String address,
            @Value("${io.fundrequest.tge.binary}") final String binary,
            final Web3j web3j) {
        return new FundRequestTokenGenerationContract(
                binary, address, web3j, Credentials.create(ECKeyPair.create(BigInteger.ONE)), BigInteger.ONE, BigInteger.ONE);
    }
}
