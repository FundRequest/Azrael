package io.fundrequest.azrael.worker.contracts.claim.sign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimSignature {
    private String platform;
    private String platformId;
    private String solver;
    private String address;
    private String r;
    private String s;
    private Integer v;
}
