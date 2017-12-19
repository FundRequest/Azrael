package io.fundrequest.azrael.worker.events.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundedEvent {
    private String transactionHash;
    private String from;
    private String platform;
    private String platformId;
    private String url;
    private String amount;

}
