package mt;

import pcap.PcapReader;
import dpi.FiveTuple;

public class PacketTask {
    public PcapReader.RawPacket packet;
    public FiveTuple tuple;

    public PacketTask(PcapReader.RawPacket packet, FiveTuple tuple) {
        this.packet = packet;
        this.tuple = tuple;
    }
}