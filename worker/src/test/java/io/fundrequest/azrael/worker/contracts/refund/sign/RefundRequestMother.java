package io.fundrequest.azrael.worker.contracts.refund.sign;

public class RefundRequestMother {

    public static RefundRequest aRefundRequest() {
        return RefundRequest.builder()
                            .address("0x35d80d4729993a4b288fd1e83bfa16b3533df524")
                            .platform("github")
                            .platformId("38")
                            .build();
    }

}