package io.fundrequest.azrael.worker.events.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundEventDto {
    private String transactionHash;
    private String logIndex;
    private String from;
    private String platform;
    private String platformId;
    private String amount;
    private long timestamp;

}
