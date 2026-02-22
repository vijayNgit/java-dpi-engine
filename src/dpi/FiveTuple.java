package dpi;

import java.util.Objects;

public class FiveTuple {

    public String srcIP, dstIP;
    public int srcPort, dstPort, protocol;

    // Packet-specific (NOT part of flow identity)
    public int payloadOffset;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FiveTuple)) return false;
        FiveTuple f = (FiveTuple) o;
        return srcIP.equals(f.srcIP) &&
               dstIP.equals(f.dstIP) &&
               srcPort == f.srcPort &&
               dstPort == f.dstPort &&
               protocol == f.protocol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcIP, dstIP, srcPort, dstPort, protocol);
    }
}