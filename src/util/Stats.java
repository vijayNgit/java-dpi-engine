package util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import dpi.AppType;

public class Stats {

    public static AtomicLong totalPackets = new AtomicLong();
    public static AtomicLong forwardedPackets = new AtomicLong();
    public static AtomicLong droppedPackets = new AtomicLong();

    // ✅ App-wise dropped packet counters
    public static Map<AppType, AtomicLong> droppedByApp =
            new ConcurrentHashMap<>();

    static {
        for (AppType app : AppType.values()) {
            droppedByApp.put(app, new AtomicLong());
        }
    }

    public static void incrementDropped(AppType app) {
        if (app == null) app = AppType.UNKNOWN;
        droppedByApp.get(app).incrementAndGet();
    }
}