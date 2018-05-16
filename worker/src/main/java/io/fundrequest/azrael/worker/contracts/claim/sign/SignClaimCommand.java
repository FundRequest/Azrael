package io.fundrequest.azrael.worker.contracts.claim.sign;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;


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
