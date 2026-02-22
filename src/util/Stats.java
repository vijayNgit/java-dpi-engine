package util;

import java.util.concurrent.atomic.AtomicLong;

public class Stats {

    public static AtomicLong totalPackets = new AtomicLong();
    public static AtomicLong forwardedPackets = new AtomicLong();
    public static AtomicLong droppedPackets = new AtomicLong();
}