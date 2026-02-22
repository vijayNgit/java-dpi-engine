import dpi.*;
import java.util.*;
import parser.PacketParser;
import pcap.*;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.println("Usage: java Main input.pcap output.pcap");
            return;
        }

        PcapReader reader = new PcapReader();
        PcapWriter writer = new PcapWriter();
        RuleManager rules = new RuleManager();

        reader.open(args[0]);
        writer.open(args[1]);

        Map<FiveTuple, Flow> flows = new HashMap<>();

        PcapReader.RawPacket pkt;
        while ((pkt = reader.nextPacket()) != null) {

            FiveTuple tuple = PacketParser.parse(pkt);
            Flow flow = flows.computeIfAbsent(tuple, k -> new Flow());

            if (!flow.blocked && tuple.dstPort == 443) {
                String sni = SNIExtractor.extract(pkt.data, 54);
                if (sni != null) {
                    flow.sni = sni;
                    if (sni.contains("youtube")) flow.app = AppType.YOUTUBE;
                    if (sni.contains("facebook")) flow.app = AppType.FACEBOOK;
                }
            }

            if (rules.isBlocked(tuple.srcIP, flow.app, flow.sni)) {
                flow.blocked = true;
            }

            if (!flow.blocked) {
                writer.writePacket(pkt);
            }
        }

        reader.close();
        writer.close();
        System.out.println("DPI processing complete.");
    }
}