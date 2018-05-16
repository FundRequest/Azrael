package io.fundrequest.azrael.worker.contracts.claim;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClaimTransaction {

    private String transactionHash;

    @Builder
    public ClaimTransaction(String transactionHash) {
        this.transactionHash = transactionHash;
    }
}
