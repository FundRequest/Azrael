package io.fundrequest.azrael.worker.contracts;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.web3j.abi.EventValues;

@AllArgsConstructor
@Data
public class ContractEvent {
    private ContractEventType eventType;
    private EventValues eventValues;
}
