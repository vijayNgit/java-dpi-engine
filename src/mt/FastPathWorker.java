package mt;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import dpi.AppType;
import dpi.Flow;
import dpi.FiveTuple;
import dpi.RuleManager;
import dpi.SNIExtractor;
import pcap.PcapReader;
import util.Stats;

public class FastPathWorker implements Runnable {

    private final BlockingQueue<PacketTask> inputQueue;
    private final BlockingQueue<PcapReader.RawPacket> outputQueue;
    private final RuleManager rules;

    // Per-thread flow table
    private final Map<FiveTuple, Flow> flows = new HashMap<>();

    // ✅ CORRECT CONSTRUCTOR
    public FastPathWorker(
            BlockingQueue<PacketTask> inputQueue,
            BlockingQueue<PcapReader.RawPacket> outputQueue,
            RuleManager rules) {

        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.rules = rules;
    }

    // ✅ Runnable requires THIS method
    @Override
    public void run() {
        try {
            while (true) {
                PacketTask task = inputQueue.take();
                if (task.isPoison()) {
                break; // graceful exit
                }
                process(task);
        }
        } catch (InterruptedException e) {
            // graceful exit
        }
    }

    private void process(PacketTask task) throws InterruptedException {

        Stats.totalPackets.incrementAndGet();

        FiveTuple tuple = task.tuple;
        Flow flow = flows.computeIfAbsent(tuple, k -> new Flow());

        // DPI logic (TLS SNI)
        if (!flow.blocked && tuple.dstPort == 443) {
            String sni = SNIExtractor.extract(
                    task.packet.data,
                    tuple.payloadOffset
            );

            if (sni != null) {
                flow.sni = sni.toLowerCase();

                if (flow.sni.contains("youtube")) {
                    flow.app = AppType.YOUTUBE;
                } else if (flow.sni.contains("facebook")) {
                    flow.app = AppType.FACEBOOK;
                }
            }
        }

        // Apply rules
        if (rules.isBlocked(tuple.srcIP, flow.app, flow.sni)) {
            flow.blocked = true;
        }

        // Forward or drop
        if (!flow.blocked) {
            Stats.forwardedPackets.incrementAndGet();
            outputQueue.put(task.packet);
        } else {
            Stats.droppedPackets.incrementAndGet();
            Stats.incrementDropped(flow.app);
        }
    }
}