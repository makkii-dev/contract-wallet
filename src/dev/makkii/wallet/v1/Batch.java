package dev.makkii.wallet.v1;

import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;
import avm.Result;
import dev.makkii.Constant;
import dev.makkii.Util;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;

/**
 * contract sends batch transactions
 */
public class Batch {

    /**
     * caller -> amount * addresses, value -> load
     *
     * conditions
     *   0. addresses.length == amounts.length
     *   1. value == amounts_total
     *
     * @return byte[]
     */
    private static byte[] send(Address[] addresses, BigInteger[] amounts){

        /**
         * conditions
         */
        int addresses_length = addresses.length;
        int amounts_length = amounts.length;
        Blockchain.require(addresses_length == amounts_length);
        Blockchain.println("!!! Batch/send condition_0 pass");
        BigInteger amounts_total = BigInteger.valueOf(0);
        for (int i = 0; i < amounts_length; i ++){
            amounts_total = amounts_total.add(amounts[i]);
        }
        Blockchain.require(Blockchain.getValue().equals(amounts_total));
        Blockchain.println("!!! Batch/send condition_1 pass");

        /**
         * transactions
         */
        for (int i = 0; i < addresses_length; i ++){
            Result result = Blockchain.call(
                addresses[i],
                amounts[i],
                Constant.BYTES_EMPTY,
                Blockchain.getRemainingEnergy()
            );
            Blockchain.require(result.isSuccess());
        }

        return Constant.BYTES_EMPTY;
    }

    // ---------------------------- main ----------------------------

    public static byte[] main() {

        byte[] data = Blockchain.getData();
        ABIDecoder decoder = new ABIDecoder(data);
        String method = decoder.decodeMethodName();
        BigInteger value = Blockchain.getValue();

        Blockchain.println("!!! Batch/main data=" + Util.bytes_to_hex(data));
        Blockchain.println("!!! Batch/main value=" + value.toString());
        Blockchain.println("!!! Batch/main method=" + method);

        switch (method) {

            case "send":
                return send(
                    decoder.decodeOneAddressArray(),
                    decoder.decodeOneBigIntegerArray()
                );

            default:
                Blockchain.println("!!! Batch/main revert no_such_method method=" + method);
                Blockchain.revert();
                return Constant.BYTES_EMPTY;
        }
    }
}