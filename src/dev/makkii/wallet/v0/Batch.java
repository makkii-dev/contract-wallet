package dev.makkii.wallet.v0;

import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;
import avm.Result;
import org.aion.avm.userlib.abi.ABIDecoder;
import dev.makkii.wallet.Constant;
import dev.makkii.wallet.Util;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;

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
            Blockchain.println("!!! Wallet/load condition_0 pass");

            Blockchain.require(amount.multiply(BigInteger.valueOf(len_addresses)).equals(Blockchain.getValue()));
            Blockchain.println("!!! Wallet/load condition_1 pass");
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
            Blockchain.println("!!! Wallet/send condition_0 pass");
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
     * caller -> batch send -> clear storage
     *
     * conditions
     *   1. caller`s storage record exists
     *
     * @return byte[]
     */
    private static byte[] withdraw(){

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
            Blockchain.println("!!! Wallet/withdraw condition_0 pass");
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

        Blockchain.println("!!! Escrow/main data=" + Util.bytes_to_hex(data));
        Blockchain.println("!!! Escrow/main value=" + value.toString());
        Blockchain.println("!!! Escrow/main method=" + method);

        switch (method) {

            case "get":
                return get();

            case "load":
                BigInteger amount = decoder.decodeOneBigInteger();
                Address[] addresses = decoder.decodeOneAddressArray();
                return load(amount, addresses);

            case "send":
                return send();

            case "withdraw":
                return withdraw();

            default:
                Blockchain.println("!!! Wallet/main revert no_such_method method=" + method);
                Blockchain.revert();
                return Constant.BYTES_EMPTY;
        }
    }
}