package io.fundrequest.azrael.worker.events.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimEventDto {
    private String transactionHash;
    private String logIndex;
    private String solverAddress;
    private String platform;
    private String platformId;
    private String solver;
    private String token;
    private String amount;
    private long timestamp;

}
