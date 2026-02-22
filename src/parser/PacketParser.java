package parser;

import pcap.PcapReader;
import dpi.FiveTuple;
import util.ByteUtils;
import util.IPUtils;

public class PacketParser {

    public static FiveTuple parse(PcapReader.RawPacket pkt) {
        byte[] d = pkt.data;

        // Ethernet header = 14 bytes
        int ipOffset = 14;

        int ipHeaderLen = (d[ipOffset] & 0x0F) * 4;
        int protocol = d[ipOffset + 9] & 0xFF;

        long srcIP = ByteUtils.readUint32(d, ipOffset + 12);
        long dstIP = ByteUtils.readUint32(d, ipOffset + 16);

        int l4Offset = ipOffset + ipHeaderLen;

        FiveTuple f = new FiveTuple();
        f.srcIP = IPUtils.intToIP(srcIP);
        f.dstIP = IPUtils.intToIP(dstIP);
        f.protocol = protocol;

        if (protocol == 6) { // TCP
            f.srcPort = ByteUtils.readUint16(d, l4Offset);
            f.dstPort = ByteUtils.readUint16(d, l4Offset + 2);

            int tcpHeaderLen = ((d[l4Offset + 12] >> 4) & 0x0F) * 4;
            f.payloadOffset = l4Offset + tcpHeaderLen;

        } else if (protocol == 17) { // UDP
            f.srcPort = ByteUtils.readUint16(d, l4Offset);
            f.dstPort = ByteUtils.readUint16(d, l4Offset + 2);
            f.payloadOffset = l4Offset + 8;
        }

        return f;
    }
}