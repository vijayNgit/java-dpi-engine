package mt;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class LoadBalancer implements Runnable {

    private final BlockingQueue<PacketTask> inputQueue;
    private final List<BlockingQueue<PacketTask>> fpQueues;

    public LoadBalancer(
            BlockingQueue<PacketTask> inputQueue,
            List<BlockingQueue<PacketTask>> fpQueues) {

        this.inputQueue = inputQueue;
        this.fpQueues = fpQueues;
    }

    @Override
    public void run() {
        try {
            while (true) {
                PacketTask task = inputQueue.take();

                if (task.isPoison()) {
                    // Forward poison to all fast paths
                    for (BlockingQueue<PacketTask> q : fpQueues) {
                        q.put(PacketTask.POISON_PILL);
                    }
                    break;
                }

                int idx = Math.abs(task.tuple.hashCode()) % fpQueues.size();
                fpQueues.get(idx).put(task);
            }
        } catch (InterruptedException e) {
            // exit
        }
    }
}