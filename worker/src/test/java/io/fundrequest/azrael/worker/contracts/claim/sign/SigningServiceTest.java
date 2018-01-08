package io.fundrequest.azrael.worker.contracts.claim.sign;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SigningServiceTest {

    @Test
    public void signMessage() {
        ClaimSigningService claimSigningService = new ClaimSigningService("b2a532bfdad1aa82ce79e2717b76abeb7eff79e18df716e35b50ea18c7f8becd");

        SignClaimCommand command = new SignClaimCommand();
        command.setAddress("0x35d80d4729993a4b288fd1e83bfa16b3533df524");
        command.setPlatform("GITHUB");
        command.setPlatformId("38");
        command.setSolver("davyvanroy");

        ClaimSignature sig = claimSigningService.signClaim(command);

        assertThat(sig.getR()).isEqualTo("0xd27d148197463f906eb351f6e536e72ae407a16793908e689c3a4f00cab9da19");
        assertThat(sig.getS()).isEqualTo("0x1ad149cd601b7e2cc319843d7679ca323fed28bab390f94294ef5a0737bbc966");
        assertThat(sig.getV()).isEqualTo(27);
        assertThat(sig.getPlatform()).isEqualTo(command.getPlatform());
        assertThat(sig.getPlatformId()).isEqualTo(command.getPlatformId());
        assertThat(sig.getAddress()).isEqualTo(command.getAddress());
        assertThat(sig.getSolver()).isEqualTo(command.getSolver());
    }
}