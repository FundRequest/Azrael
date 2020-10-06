package io.fundrequest.azrael.worker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

import java.io.IOException;
import java.net.URI;

@Configuration
public class Web3Config {

    @Bean
    Web3j provideWeb3J(@Value("${io.fundrequest.azrael.worker.endpoint.url}") final String endpoint) throws IOException {
        if (endpoint.startsWith("http")) {
            return Web3j.build(new HttpService(endpoint));
        }
        WebSocketService web3jService = new WebSocketService(endpoint, true);
        web3jService.connect();
        return Web3j.build(web3jService);

    }
}
