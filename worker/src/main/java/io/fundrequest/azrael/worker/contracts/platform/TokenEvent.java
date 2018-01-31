package io.fundrequest.azrael.worker.contracts.platform;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.web3j.abi.EventValues;

@AllArgsConstructor
@Data
public class TokenEvent {
    private TokenEventType eventType;
    private EventValues eventValues;
}
