package mt;

import java.util.concurrent.BlockingQueue;
import pcap.PcapReader;
import pcap.PcapWriter;

public class OutputWriter implements Runnable {

    private final BlockingQueue<PcapReader.RawPacket> queue;
    private final PcapWriter writer;

    public OutputWriter(
            BlockingQueue<PcapReader.RawPacket> queue,
            PcapWriter writer) {

        this.queue = queue;
        this.writer = writer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                PcapReader.RawPacket pkt = queue.take();

                if (pkt == PcapReader.RawPacket.POISON_PILL) {
                    break; // poison received
                }

                writer.writePacket(pkt);
            }
        } catch (Exception e) {
            // exit
        } finally {
            try {
                writer.close();
            } catch (Exception ignored) {}
        }
    }
}