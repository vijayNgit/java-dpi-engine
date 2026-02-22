package pcap;

import java.io.*;

public class PcapWriter {

    private DataOutputStream dos;

    public void open(String file) throws IOException {
        dos = new DataOutputStream(new FileOutputStream(file));
        writeGlobalHeader();
    }

    private void writeGlobalHeader() throws IOException {
        dos.writeInt(Integer.reverseBytes(0xa1b2c3d4));
        dos.writeShort(Short.reverseBytes((short) 2));
        dos.writeShort(Short.reverseBytes((short) 4));
        dos.writeInt(0);
        dos.writeInt(0);
        dos.writeInt(Integer.reverseBytes(65535));
        dos.writeInt(Integer.reverseBytes(1));
    }

    public void writePacket(PcapReader.RawPacket pkt) throws IOException {
        dos.writeInt(Integer.reverseBytes(pkt.tsSec));
        dos.writeInt(Integer.reverseBytes(pkt.tsUsec));
        dos.writeInt(Integer.reverseBytes(pkt.inclLen));
        dos.writeInt(Integer.reverseBytes(pkt.origLen));
        dos.write(pkt.data);
    }

    public void close() throws IOException {
        dos.close();
    }
}
