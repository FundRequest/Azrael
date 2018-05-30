package io.fundrequest.azrael.worker.events.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaidEventDto {

    private String transactionHash;
    private String logIndex;
    private String beneficiary;
    private String weiAmount;
    private String tokenAmount;
    private Long timestamp;
    private boolean personalCapActive;

}
