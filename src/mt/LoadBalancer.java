package mt;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class LoadBalancer implements Runnable {

    private final BlockingQueue<PacketTask> inputQueue;
    private final List<BlockingQueue<PacketTask>> fpQueues;

    public LoadBalancer(
            BlockingQueue<PacketTask> in,
            List<BlockingQueue<PacketTask>> fps) {

        this.inputQueue = in;
        this.fpQueues = fps;
    }

    @Override
    public void run() {
        try {
            while (true) {
                PacketTask task = inputQueue.take();
                int idx = Math.abs(task.tuple.hashCode()) % fpQueues.size();
                fpQueues.get(idx).put(task);
            }
        } catch (InterruptedException e) {
            // shutdown
        }
    }
}