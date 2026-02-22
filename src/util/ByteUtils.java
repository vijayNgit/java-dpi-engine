package util;

public class ByteUtils {

    public static int readUint16(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 8) |
               (data[offset + 1] & 0xFF);
    }

    public static long readUint32(byte[] data, int offset) {
        return ((long)(data[offset] & 0xFF) << 24) |
               ((long)(data[offset + 1] & 0xFF) << 16) |
               ((long)(data[offset + 2] & 0xFF) << 8) |
               ((long)(data[offset + 3] & 0xFF));
    }
}