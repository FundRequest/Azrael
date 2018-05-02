package io.fundrequest.azrael.worker.contracts.claim.sign;

import org.junit.Test;
import org.web3j.abi.datatypes.generated.Bytes32;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.util.Arrays;

public class ClaimServiceTest {

    @Test
    public void name() {
        //        fout: 3078323863356566663832333437306563653933326564323733323961303461
        //         juist: 28c5eff823470ece932ed27329a04a241cb11db1900738ec669b8a28cec42a23

//        byte[] byteValue = "0x28c5eff823470ece932ed27329a04a241cb11db1900738ec669b8a28cec42a23".getBytes();
//        byte[] byteValueLen32 = new byte[32];
//        System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
//        Bytes32 result1 = new Bytes32(byteValueLen32);


        String s="28c5eff823470ece932ed27329a04a241cb11db1900738ec669b8a28cec42a23";
        byte[] bytes = Arrays.copyOf(DatatypeConverter.parseHexBinary(s), 32);


        //        String hexValue1 = javax.xml.bind.DatatypeConverter.printHexBinary(result1.getValue());
        Bytes32 result2 = new Bytes32(bytes);
        String hexValue2 = javax.xml.bind.DatatypeConverter.printHexBinary(result2.getValue());
//        System.out.println(hexValue1);
        System.out.println(hexValue2);
    }
}