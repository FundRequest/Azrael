package io.fundrequest.azrael.worker.config;

import io.fundrequest.azrael.worker.contracts.crowdsale.FundRequestTokenGenerationContract;
import io.fundrequest.azrael.worker.contracts.platform.FundRequestContract;
import io.fundrequest.azrael.worker.contracts.platform.FundRequestToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;

import java.math.BigInteger;

@Configuration
public class ContractsConfig {

    @Bean
    @ConditionalOnProperty(name = "io.fundrequest.contract.address")
    public FundRequestContract providePlatformContract(
            @Value("${io.fundrequest.contract.address}") final String address,
            final Web3j web3j) {
        return new FundRequestContract(address, web3j, Credentials.create(ECKeyPair.create(BigInteger.ONE)), BigInteger.ONE, BigInteger.ONE);
    }

    @Bean
    @ConditionalOnProperty(name = "io.fundrequest.tge.address")
    public FundRequestTokenGenerationContract provideTokenGenerationContract(
            @Value("${io.fundrequest.tge.address}") final String address,
            final Web3j web3j) {
        return new FundRequestTokenGenerationContract(address, web3j, Credentials.create(ECKeyPair.create(BigInteger.ONE)), BigInteger.ONE, BigInteger.ONE);
    }

    @Bean
    @ConditionalOnProperty(name = "io.fundrequest.token.address")
    public FundRequestToken provideTokenContract(
            @Value("${io.fundrequest.token.address}") final String address,
            final Web3j web3j) {
        return new FundRequestToken(address, web3j, Credentials.create(ECKeyPair.create(BigInteger.ONE)), BigInteger.ONE, BigInteger.ONE);
    }
}
