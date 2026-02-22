import dpi.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import mt.*;
import parser.PacketParser;
import pcap.*;
import util.Stats;

public class MainMT {

    static final int LB_COUNT = 2;
    static final int FP_COUNT = 4;

    // Global stats
    public static AtomicLong totalPackets = new AtomicLong();
    public static AtomicLong forwardedPackets = new AtomicLong();
    public static AtomicLong droppedPackets = new AtomicLong();

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.println("Usage: java MainMT input.pcap output.pcap [rules]");
            return;
        }

        RuleManager rules = new RuleManager();

        // -------- CLI PARSING (C++ style) --------
        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "--block-app":
                    String app = args[++i].toUpperCase();
                    rules.blockApp(AppType.valueOf(app));
                    System.out.println("[Rules] Blocked app: " + app);
                    break;

                case "--block-domain":
                    String domain = args[++i];
                    rules.blockDomain(domain);
                    System.out.println("[Rules] Blocked domain: " + domain);
                    break;

                case "--block-ip":
                    String ip = args[++i];
                    rules.blockIP(ip);
                    System.out.println("[Rules] Blocked IP: " + ip);
                    break;
            }
        }

        PcapReader reader = new PcapReader();
        PcapWriter writer = new PcapWriter();

        reader.open(args[0]);
        writer.open(args[1]);

        System.out.println("Opened PCAP file: " + args[0]);
        System.out.println("[Reader] Processing packets...");

        BlockingQueue<PacketTask> readerQueue = new LinkedBlockingQueue<>();
        BlockingQueue<PcapReader.RawPacket> outputQueue = new LinkedBlockingQueue<>();

        // Fast Paths
        List<BlockingQueue<PacketTask>> fpQueues = new ArrayList<>();
        for (int i = 0; i < FP_COUNT; i++) {
            BlockingQueue<PacketTask> q = new LinkedBlockingQueue<>();
            fpQueues.add(q);
            new Thread(
                new FastPathWorker(q, outputQueue, rules),
                "FP-" + i
            ).start();
        }

        // Load Balancers
        for (int i = 0; i < LB_COUNT; i++) {
            new Thread(
                new LoadBalancer(readerQueue, fpQueues),
                "LB-" + i
            ).start();
        }

        // Output writer
        new Thread(new OutputWriter(outputQueue, writer), "Writer").start();

        // Reader loop
        PcapReader.RawPacket pkt;
        while ((pkt = reader.nextPacket()) != null) {
            FiveTuple tuple = PacketParser.parse(pkt);
            readerQueue.put(new PacketTask(pkt, tuple));
        }

        System.out.println("[Reader] Done reading packets");

        // Allow workers to finish
        Thread.sleep(2000);

        System.out.println("\n========== PROCESSING REPORT ==========");
        System.out.println("Total Packets:   " + Stats.totalPackets.get());
        System.out.println("Forwarded:       " + Stats.forwardedPackets.get());
        System.out.println("Dropped:         " + Stats.droppedPackets.get());
        System.out.println("======================================");
    }
}