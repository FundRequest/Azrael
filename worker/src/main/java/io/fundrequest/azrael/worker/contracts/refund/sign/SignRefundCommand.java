package io.fundrequest.azrael.worker.contracts.refund.sign;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;


@Data
@EqualsAndHashCode
public class SignRefundCommand {

    @NotEmpty
    private String platform;
    @NotEmpty
    private String platformId;
    @NotEmpty
    private String address;
}
