package io.fundrequest.azrael.worker.contracts.refund;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RefundTransaction {

    private String transactionHash;

    @Builder
    public RefundTransaction(String transactionHash) {
        this.transactionHash = transactionHash;
    }
}
