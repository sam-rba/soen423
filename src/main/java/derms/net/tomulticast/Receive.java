package derms.net.tomulticast;

import derms.net.MessagePayload;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Receive messages from the multicast group and place them in the holdback queue.
 * Once previous messages are received, they are transferred from the holdback queue
 * to the delivery queue in total order.
 */
class Receive<T extends MessagePayload> extends TotalOrderMulticast<T> implements Runnable {
    private final PriorityBlockingQueue<Message<T>> holdback;
    private final BlockingQueue<Message<T>> deliver;

    Receive(InetSocketAddress group, InetAddress laddr, BlockingQueue<Message<T>> deliver) throws IOException {
        super(group, laddr);
        this.holdback = new PriorityBlockingQueue<Message<T>>();
        this.deliver = deliver;
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Message<T> msg = sock.receive();
                holdback.put(msg);
                tryDeliver();
            }
        } catch (InterruptedException e) {
            close();
        }
    }

    // Try to move messages from the holdback queue to the delivery queue.
    private void tryDeliver() {
        while (!holdback.isEmpty()) {
            // Messages with lower sequence numbers are removed from the priority queue first.
            Message<T> msg = holdback.peek();
            if (msg == null || msg.seq != seq)
                return;

            // This is the next element in the sequence; deliver it.
            msg = holdback.remove();
            deliver.add(msg);
            incSeq();
        }
    }
}
