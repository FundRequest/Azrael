package io.fundrequest.azrael.worker.contracts.refund.sign;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;

import java.math.BigInteger;

@Component
@Slf4j
public class RefundSigningService {

    private final ECKeyPair keyPair;

    public RefundSigningService(@Value("${io.fundrequest.sign.account}") String signingAccount) {
        this.keyPair = getPrivateKey(signingAccount);
    }

    public RefundSignature signClaim(SignRefundCommand command) {
        String plainMessage = createMessageToSign(command);

        Sign.SignatureData signMessage = Sign.signMessage(plainMessage.getBytes(), keyPair);
        String r = "0x" + Hex.encodeHexString(signMessage.getR());
        String s = "0x" + Hex.encodeHexString(signMessage.getS());
        return new RefundSignature.RefundSignatureBuilder()
                .platform(command.getPlatform())
                .platformId(command.getPlatformId())
                .address(command.getAddress().toLowerCase())
                .r(r)
                .s(s)
                .v((int) signMessage.getV())
                .build();
    }

    private String createMessageToSign(SignRefundCommand command) {
        return command.getPlatform() + "_" + command.getPlatformId();
    }

    private ECKeyPair getPrivateKey(String signingAccount) {
        BigInteger key = new BigInteger(signingAccount, 16);
        return ECKeyPair.create(key.toByteArray());
    }
}
