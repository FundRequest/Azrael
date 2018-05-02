package io.fundrequest.azrael.worker.contracts.claim.sign;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

@Data
@EqualsAndHashCode
public class SignClaimCommand {

    @NotEmpty
    private String platform;
    @NotEmpty
    private String platformId;
    @NotEmpty
    private String solver;
    @NotEmpty
    private String address;
}
