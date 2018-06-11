package io.fundrequest.azrael.worker.contracts.refund.sign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {
    private String platform;
    private String platformId;
    private String address;
}
