package pcap;

import java.io.*;

public class PcapReader {

    private DataInputStream dis;

    public void open(String file) throws IOException {
        dis = new DataInputStream(new FileInputStream(file));
        readGlobalHeader();
    }

    private void readGlobalHeader() throws IOException {
        int magic = readIntLE();
        if (magic != 0xa1b2c3d4 && magic != 0xd4c3b2a1)
            throw new IOException("Invalid PCAP file");

        readShortLE(); // version major
        readShortLE(); // version minor
        readIntLE();   // thiszone
        readIntLE();   // sigfigs
        readIntLE();   // snaplen
        readIntLE();   // network
    }

    public RawPacket nextPacket() throws IOException {
        try {
            int tsSec = readIntLE();
            int tsUsec = readIntLE();
            int inclLen = readIntLE();
            int origLen = readIntLE();

            byte[] data = new byte[inclLen];
            dis.readFully(data);

            return new RawPacket(tsSec, tsUsec, inclLen, origLen, data);
        } catch (EOFException e) {
            return null;
        }
    }

    public void close() throws IOException {
        dis.close();
    }

    private int readIntLE() throws IOException {
        return Integer.reverseBytes(dis.readInt());
    }

    private short readShortLE() throws IOException {
        return Short.reverseBytes(dis.readShort());
    }

    public static class RawPacket {
    public int tsSec, tsUsec, inclLen, origLen;
    public byte[] data;

    // ✅ Writer poison pill
    public static final RawPacket POISON_PILL =
            new RawPacket(-1, -1, -1, -1, null);

    public RawPacket(int tsSec, int tsUsec, int inclLen, int origLen, byte[] data) {
        this.tsSec = tsSec;
        this.tsUsec = tsUsec;
        this.inclLen = inclLen;
        this.origLen = origLen;
        this.data = data;
    }
}
}