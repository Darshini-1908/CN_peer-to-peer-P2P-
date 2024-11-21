import java.nio.ByteBuffer;

/**
 * Utility class containing common methods.
 */
public class PeerProcessUtils {
/**
     * Converts an integer to a byte array.
     *
     * @param dav_value - value to convert
     * @return byte array value of integer
     */
    public static byte[] convertIntToByteArray(int dav_value) {
        return ByteBuffer.allocate(4).putInt(dav_value).array();
    }

    /**
     * Converts a byte array to an integer.
     *
     * @param dav_data_inB - value to convert
     * @return integer value of byte array
     */
    public static int convertByteArrayToInt(byte[] dav_data_inB) {
        return ByteBuffer.wrap(dav_data_inB).getInt();
    }
}
