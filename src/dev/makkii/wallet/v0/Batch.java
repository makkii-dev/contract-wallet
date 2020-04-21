package dev.makkii.wallet.v0;

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
 *
 * storage
 * key: address
 * value: total_amount(BigInteger) + addresses(BigInteger[]) + amounts(BigInteger[])
 */
public class Batch {

    /**
     * caller -> get existing batch record
     *
     * @return byte[]
     */
    private static byte[] get(){

        /**
         * key
         */
        byte[] key = Blockchain.getCaller().toByteArray();

        return Blockchain.getStorage(key);
    }

    /**
     * host -> amounts -> load
     *
     * conditions
     *   0. record does not exist
     *   1. addresses.length == amounts.length
     *   2. total == value
     *
     * @param addresses Address[]
     * @param amounts BigInteger[]
     * @return byte[]
     */
    private static byte[] load(Address[] addresses, BigInteger[] amounts){

        /**
         * key
         */
        byte[] key = Blockchain.getCaller().toByteArray();

        /**
         * conditions
         */
        Blockchain.require(Blockchain.getStorage(key) == null);
        Blockchain.println("!!! Batch/load condition_0 pass");
        int len_addresses = addresses.length;
        int len_amounts = amounts.length;
        Blockchain.require(len_addresses == len_amounts);
        Blockchain.println("!!! Batch/load condition_1 pass");
        BigInteger total = BigInteger.valueOf(0);
        for(int i = 0, m = amounts.length; i < m; i++){
            total = total.add(amounts[i]);
        }
        Blockchain.require(total.equals(Blockchain.getValue()));
        Blockchain.println("!!! Batch/load condition_2 pass");

        /**
         * storage
         */
        int len = Util.get_big_integer_length() + Util.get_address_length() * len_addresses + Util.get_big_integer_length() * len_addresses;
        byte[] data = new byte[len];
        ABIStreamingEncoder encoder = new ABIStreamingEncoder(data);
        encoder.encodeOneBigInteger(total);
        encoder.encodeOneAddressArray(addresses);
        encoder.encodeOneBigIntegerArray(amounts);
        Blockchain.putStorage(key, data);

        return Constant.BYTES_EMPTY;
    }

    /**
     * caller -> amount * addresses, value -> load
     *
     * conditions
     *   0. record exists
     *   1. value == 0
     *
     * @return byte[]
     */
    private static byte[] send(){

        byte[] key = Blockchain.getCaller().toByteArray();
        byte[] data = Blockchain.getStorage(key);

        ABIDecoder decoder = new ABIDecoder(data);
        decoder.decodeOneBigInteger();
        Address[] addresses = decoder.decodeOneAddressArray();
        BigInteger[] amounts = decoder.decodeOneBigIntegerArray();

        /**
         * conditions
         */
        {
            Blockchain.require(data != null);
            Blockchain.println("!!! Batch/send condition_0 pass");

            Blockchain.require(Blockchain.getValue().equals(Constant.BN_ZERO));
            Blockchain.println("!!! Batch/send condition_1 pass");
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
                amounts[i],
                Constant.BYTES_EMPTY,
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
     *   0. caller`s storage record exists
     *   1. value == 0
     *
     * @return byte[]
     */
    private static byte[] cancel(){

        Address caller = Blockchain.getCaller();
        byte[] key = caller.toByteArray();

        byte[] data = Blockchain.getStorage(key);
        ABIDecoder decoder = new ABIDecoder(data);
        BigInteger total = decoder.decodeOneBigInteger();

        /**
         * conditions
         */
        {
            Blockchain.require(data != null);
            Blockchain.println("!!! Batch/cancel condition_0 pass");

            Blockchain.require(Blockchain.getValue().equals(Constant.BN_ZERO));
            Blockchain.println("!!! Batch/cancel condition_1 pass");
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
            total,
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
                return load(
                    decoder.decodeOneAddressArray(),
                    decoder.decodeOneBigIntegerArray()
                );

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