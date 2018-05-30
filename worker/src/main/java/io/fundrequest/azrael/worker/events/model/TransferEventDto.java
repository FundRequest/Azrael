package io.fundrequest.azrael.worker.events.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferEventDto {

    private String transactionHash;
    private String logIndex;
    private String from;
    private String to;
    private String amount;
    private long timestamp;

}
