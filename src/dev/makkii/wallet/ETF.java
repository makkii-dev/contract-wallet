package dev.makkii.wallet;

import avm.Address;
import avm.Blockchain;
import dev.makkii.Constant;
import dev.makkii.Util;
import org.aion.avm.userlib.abi.ABIDecoder;

import java.math.BigInteger;

/**
 * contract simulates etf(email transfer)
 */
public class ETF {

    private static Address management;
    private static byte fee;
    private static byte max;
    static {
        management = Blockchain.getCaller();
        max = 10;
        fee = 1;
    }

    // ---------------------------- standard operations ----------------------------

    /**
     * caller -> get etf records
     *
     * conditions
     *   1. value == 0
     *
     * @return byte[]
     */
    private static byte[] get(){

        /**
         * conditions
         */
        {
            Blockchain.require(Blockchain.getValue().equals(Constant.BN_ZERO));
        }

        byte[] key = Blockchain.getCaller().toByteArray();

        return Blockchain.getStorage(key);
    }

    /**
     * caller -> create -> etf
     * @param hash_email byte[]
     * @param hash_secret byte[]
     * @param amount BigInteger
     * @return byte[]
     */
    private static byte[] create(byte[] hash_email, byte[] hash_secret, BigInteger amount){

        byte[] key = Blockchain.getCaller().toByteArray();
        BigInteger value = Blockchain.getValue();

        /**
         * storage
         */


        return Constant.BYTES_EMPTY;
    }

    /**
     *
     * @return byte[]
     */
    private static byte[] cancel(byte id){
        return Constant.BYTES_EMPTY;
    }

    // ---------------------------- management operations ----------------------------

    private static byte[] max(){
        return new byte[] { max };
    }

    /**
     * management -> update_max
     *
     * conditions
     *   1. management == caller
     *   2. value == 0
     *
     * @param max_new byte
     * @return byte[0]
     */
    private static byte[] update_max(byte max_new){

        /**
         * conditions
         */
        {
            Blockchain.require(management.equals(Blockchain.getCaller()));
            Blockchain.println("!!! Email/update_max condition_0 pass");
        }

        max = max_new;

        return Constant.BYTES_EMPTY;
    }

    private static byte[] management(){
        return management.toByteArray();
    }

    private static byte[] update_management(Address management_new){

        return Constant.BYTES_EMPTY;
    }

    // ---------------------------- main ----------------------------

    public static byte[] main() {

        byte[] data = Blockchain.getData();
        ABIDecoder decoder = new ABIDecoder(data);
        String method = decoder.decodeMethodName();
        BigInteger value = Blockchain.getValue();

        Blockchain.println("!!! Email/main data=" + Util.bytes_to_hex(data));
        Blockchain.println("!!! Email/main value=" + value.toString());
        Blockchain.println("!!! Email/main method=" + method);

        switch (method) {

            case "get":
                return get();

//            case "create":
//                return create();

//            case "withdraw":
//                BigInteger amount = decoder.decodeOneBigInteger();
//                Address[] addresses = decoder.decodeOneAddressArray();
//                return (amount, addresses);
//
//            case "cancel":
//                return cancel();




            default:
                Blockchain.println("!!! Batch/main revert no_such_method method=" + method);
                Blockchain.revert();
                return Constant.BYTES_EMPTY;
        }
    }

}