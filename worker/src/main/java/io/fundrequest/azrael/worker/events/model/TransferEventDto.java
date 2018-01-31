package io.fundrequest.azrael.worker.events.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferEventDto {

    private String transactionHash;
    private String amount;
    private String from;
    private String to;
    private long timestamp;

}
