package li.itcc.hackathon15.json;

public class SerializationHelper {

    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String encodeByteArrayToHex(byte[] data) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_UPPER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_UPPER[0x0F & data[i]];
        }
        return new String(out);
    }

    public static byte[] decodeHexToByteArray(String str) {
        char[] data = str.toCharArray();
        return decodeHexToByteArray(data);
    }

    private static byte[] decodeHexToByteArray(char[] data) {
        int len = data.length;

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = Character.digit(data[j], 16) << 4;
            j++;
            f = f | Character.digit(data[j], 16);
            j++;
            out[i] = (byte)(f & 0xFF);
        }

        return out;
    }

}
