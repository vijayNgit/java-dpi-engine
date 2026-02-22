package mt;

import pcap.PcapReader;
import dpi.FiveTuple;

public class PacketTask {

    public static final PacketTask POISON_PILL = new PacketTask(null, null);

    public PcapReader.RawPacket packet;
    public FiveTuple tuple;

    public PacketTask(PcapReader.RawPacket packet, FiveTuple tuple) {
        this.packet = packet;
        this.tuple = tuple;
    }

    public boolean isPoison() {
        return packet == null && tuple == null;
    }
}