package io.fundrequest.azrael.worker.contracts.crowdsale;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.web3j.abi.EventValues;

@AllArgsConstructor
@Data
public class PaidEvent {

    private EventValues eventValues;

}
