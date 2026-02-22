package mt;

import java.util.concurrent.BlockingQueue;
import pcap.PcapReader;   // ✅ REQUIRED
import pcap.PcapWriter;

public class OutputWriter implements Runnable {

    private final BlockingQueue<PcapReader.RawPacket> queue;
    private final PcapWriter writer;

    public OutputWriter(
            BlockingQueue<PcapReader.RawPacket> q,
            PcapWriter w) {

        this.queue = q;
        this.writer = w;
    }

    @Override
    public void run() {
        try {
            while (true) {
                PcapReader.RawPacket pkt = queue.take();
                writer.writePacket(pkt);
            }
        } catch (Exception e) {
            // shutdown
        }
    }
}