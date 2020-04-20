
package dev.makkii.wallet;

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
     * caller -> get existing batch record
     *
     * @return byte[]
     */
    private static byte[] get(){

        byte[] key = Blockchain.getCaller().toByteArray();

        return Blockchain.getStorage(key);
    }

    /**
     * host -> amount * addresses -> load
     *
     * conditions
     *   1. caller`s storage record does not exist
     *   2. amount * addresses.length == value
     *
     * @param amount BigInteger
     * @param addresses Address[]
     * @return byte[]
     */
    private static byte[] load(BigInteger amount, Address[] addresses){

        byte[] key = Blockchain.getCaller().toByteArray();
        int len_addresses = addresses.length;

        /**
         * conditions
         */
        {
            Blockchain.require(Blockchain.getStorage(key) == null);
            Blockchain.println("!!! Batch/load condition_0 pass");

            Blockchain.require(amount.multiply(BigInteger.valueOf(len_addresses)).equals(Blockchain.getValue()));
            Blockchain.println("!!! Batch/load condition_1 pass");
        }

        /**
         * storage
         */
        int len = Util.get_big_integer_length() + Util.get_address_length() * len_addresses;
        byte[] data = new byte[len];
        ABIStreamingEncoder encoder = new ABIStreamingEncoder(data);
        encoder.encodeOneBigInteger(amount);
        encoder.encodeOneAddressArray(addresses);
        Blockchain.putStorage(key, data);

        return Constant.BYTES_EMPTY;
    }

    /**
     * caller -> amount * addresses, value -> load
     *
     * conditions
     *   1. caller`s storage record exists
     *
     * @return byte[]
     */
    private static byte[] send(){

        byte[] key = Blockchain.getCaller().toByteArray();
        byte[] data = Blockchain.getStorage(key);

        ABIDecoder decoder = new ABIDecoder(data);
        BigInteger amount = decoder.decodeOneBigInteger();
        Address[] addresses = decoder.decodeOneAddressArray();

        /**
         * conditions
         */
        {
            Blockchain.require(data != null);
            Blockchain.println("!!! Batch/send condition_0 pass");
        }

        /**
         * storage
         */
        Blockchain.putStorage(key, null);

        /**
         * transactions
         */
        for (int i = 0, m = addresses.length; i < m; i ++){
            Result result = Blockchain.call(
                addresses[i],
                amount,
                null,
                Blockchain.getRemainingEnergy()
            );
            Blockchain.require(result.isSuccess());
        }

        return Constant.BYTES_EMPTY;
    }

    /**
     * caller -> cancel -> refund value & clear storage
     *
     * conditions
     *   1. caller`s storage record exists
     *
     * @return byte[]
     */
    private static byte[] cancel(){

        Address caller = Blockchain.getCaller();
        byte[] key = caller.toByteArray();

        byte[] data = Blockchain.getStorage(key);
        ABIDecoder decoder = new ABIDecoder(data);
        BigInteger amount = decoder.decodeOneBigInteger();
        Address[] addresses = decoder.decodeOneAddressArray();
        BigInteger amount_total = amount.multiply(BigInteger.valueOf(addresses.length));

        /**
         * conditions
         */
        {
            Blockchain.require(data != null);
            Blockchain.println("!!! Batch/withdraw condition_0 pass");
        }

        /**
         * storage
         */
        Blockchain.putStorage(key, null);

        /**
         * transaction
         */
        Result result = Blockchain.call(
            caller,
            amount_total,
            null,
            Blockchain.getRemainingEnergy()
        );
        Blockchain.require(result.isSuccess());

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

            case "get":
                return get();

            case "load":
                BigInteger amount = decoder.decodeOneBigInteger();
                Address[] addresses = decoder.decodeOneAddressArray();
                return load(amount, addresses);

            case "send":
                return send();

            case "cancel":
                return cancel();

            default:
                Blockchain.println("!!! Batch/main revert no_such_method method=" + method);
                Blockchain.revert();
                return Constant.BYTES_EMPTY;
        }
    }
}