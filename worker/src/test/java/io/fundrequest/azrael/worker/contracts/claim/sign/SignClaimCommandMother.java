package io.fundrequest.azrael.worker.contracts.claim.sign;

public final class SignClaimCommandMother {

    public static SignClaimCommand aSignClaimCommand() {
        SignClaimCommand signClaimCommand = new SignClaimCommand();
        signClaimCommand.setAddress("0x35d80d4729993a4b288fd1e83bfa16b3533df524");
        signClaimCommand.setSolver("davyvanroy");
        signClaimCommand.setPlatform("GITHUB");
        signClaimCommand.setPlatformId("38");
        return signClaimCommand;
    }

}