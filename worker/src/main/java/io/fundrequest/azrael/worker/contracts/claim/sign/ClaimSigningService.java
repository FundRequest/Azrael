package io.fundrequest.azrael.worker.contracts.claim.sign;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;

import java.math.BigInteger;

@Component
public class ClaimSigningService {

    private String signingAccount;

    public ClaimSigningService(@Value("${io.fundrequest.sign.account}") String signingAccount) {
        this.signingAccount = signingAccount;
    }

    public ClaimSignature signClaim(SignClaimCommand command) {
        String plainMessage = createMessageToSign(command);
        BigInteger key = new BigInteger(signingAccount, 16);
        ECKeyPair ecKeyPair = ECKeyPair.create(key.toByteArray());
        Sign.SignatureData signMessage = Sign.signMessage(plainMessage.getBytes(), ecKeyPair);
        String r = "0x" + Hex.encodeHexString(signMessage.getR());
        String s = "0x" + Hex.encodeHexString(signMessage.getS());
        return new ClaimSignature.ClaimSignatureBuilder()
                .platform(command.getPlatform())
                .platformId(command.getPlatformId())
                .solver(command.getSolver())
                .address(command.getAddress())
                .r(r)
                .s(s)
                .v((int) signMessage.getV())
                .build();
    }

    private String createMessageToSign(SignClaimCommand command) {
        return command.getPlatform() + "_" + command.getPlatformId() + "_" + command.getSolver() + "_" + command.getAddress();
    }
}
