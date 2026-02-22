package dpi;

import util.ByteUtils;

public class SNIExtractor {

    public static String extract(byte[] data, int offset) {

        if (offset + 5 >= data.length) return null;

        // TLS Handshake
        if (data[offset] != 0x16) return null;

        // Client Hello
        if (data[offset + 5] != 0x01) return null;

        int pos = offset + 43;
        if (pos >= data.length) return null;

        // Session ID
        int sessionLen = data[pos] & 0xFF;
        pos += 1 + sessionLen;
        if (pos + 2 >= data.length) return null;

        // Cipher suites
        int cipherLen = ByteUtils.readUint16(data, pos);
        pos += 2 + cipherLen;
        if (pos >= data.length) return null;

        // Compression
        int compLen = data[pos] & 0xFF;
        pos += 1 + compLen;
        if (pos + 2 >= data.length) return null;

        // Extensions
        int extLen = ByteUtils.readUint16(data, pos);
        pos += 2;

        int end = Math.min(pos + extLen, data.length);

        while (pos + 4 <= end) {
            int type = ByteUtils.readUint16(data, pos);
            int len = ByteUtils.readUint16(data, pos + 2);
            pos += 4;

            if (type == 0x0000 && pos + len <= data.length) {
                int sniLen = ByteUtils.readUint16(data, pos + 3);
                if (pos + 5 + sniLen <= data.length) {
                    return new String(data, pos + 5, sniLen);
                }
            }
            pos += len;
        }
        return null;
    }
}