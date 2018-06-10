package io.fundrequest.azrael.worker.contracts.refund.sign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundSignature {
    private String platform;
    private String platformId;
    private String address;
    private String r;
    private String s;
    private Integer v;
}
