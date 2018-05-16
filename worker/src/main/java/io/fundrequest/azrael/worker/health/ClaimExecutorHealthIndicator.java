package io.fundrequest.azrael.worker.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.math.BigInteger;

@Component
@Slf4j
public class ClaimExecutorHealthIndicator extends AbstractHealthIndicator {

    private final Web3j web3j;
    private String executorAddress;

    public ClaimExecutorHealthIndicator(final Web3j web3j,
                                        final @Value("${io.fundrequest.execute.account}") String claimExecutorAccount) {
        this.web3j = web3j;
        this.executorAddress = prettify(Keys.getAddress(getPrivateKey(claimExecutorAccount)));
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        hasEnoughBalance();
        builder.up();
    }

    private void hasEnoughBalance() {
        BigInteger balance;
        try {
            balance = web3j.ethGetBalance(executorAddress, DefaultBlockParameterName.LATEST).send().getBalance();
        } catch (final Exception ex) {
            throw new IllegalArgumentException("problem trying to fetch balance");
        }

        if (balance.compareTo(BigInteger.ONE.multiply(BigInteger.valueOf((long) Math.pow(10, 18)))) < 0) {
            throw new IllegalArgumentException("Balance is lower than 1 eth");
        }
    }

    private ECKeyPair getPrivateKey(String signingAccount) {
        BigInteger key = new BigInteger(signingAccount, 16);
        return ECKeyPair.create(key.toByteArray());
    }

    private String prettify(final String address) {
        if (!address.startsWith("0x")) {
            return String.format("0x%s", address);
        } else {
            return address;
        }
    }


}
