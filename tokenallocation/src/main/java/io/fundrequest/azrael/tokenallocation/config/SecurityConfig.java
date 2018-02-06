package io.fundrequest.azrael.tokenallocation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Numeric;

@Component
public class SecurityConfig {

    @Value("${io.fundrequest.private-key}")
    private String privateKey;

    public Credentials getKey() {
        return Credentials.create(ECKeyPair.create(Numeric.decodeQuantity(privateKey)));
    }
}
