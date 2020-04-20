package dev.makkii;

public class Util {

    static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    /**
     * @param input byte[]
     * @return String
     */
    public static String bytes_to_hex(byte[] input) {

        int length = input.length;
        char[] chars = new char[length * 2];
        for(int i = 0; i < length; ++i) {
            int v = input[i] & 255;
            chars[i * 2] = HEX_ARRAY[v >>> 4];
            chars[i * 2 + 1] = HEX_ARRAY[v & 15];
        }
        return new String(chars);
    }

    /**
     * (byte) token + address length = 1 + 32
     * @return int
     */
    public static int get_address_length(){
        return 33;
    }

    /**
     * (byte) token + (byte) length + max big integer length = 1 + 1 + 32
     * @return int
     */
    public static int get_big_integer_length(){
        return 34;
    }
}
