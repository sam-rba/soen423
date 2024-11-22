package derms.net.tomulticast;

import derms.net.MessagePayload;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.logging.Logger;

/** One of the receiving processes in {@link TotalOrderMulticast} group. */
public class TotalOrderMulticastReceiver<T extends MessagePayload> {
    private static final Duration terminationTimeout = Duration.ofSeconds(1);

    private final BlockingQueue<Message<T>> deliver;
    private final Logger log;
    private final ExecutorService pool;

    /**
     * Join the specified totally-ordered multicast group as a receiver.
     *
     * @param group The IP address and port of the multicast group.
     * @param laddr The IP address of the local process.
     */
    public TotalOrderMulticastReceiver(InetSocketAddress group, InetAddress laddr) throws IOException {
        this.deliver = new LinkedBlockingQueue<Message<T>>();
        this.log = Logger.getLogger(getClass().getName());

        this.pool = Executors.newSingleThreadExecutor();
        pool.execute(new Receive<T>(group, laddr, deliver));
    }

    /** Close the underlying socket. */
    public void close() {
        pool.shutdownNow();
        try {
            if (!pool.awaitTermination(terminationTimeout.toMillis(), TimeUnit.MILLISECONDS))
                log.warning("Thread pool did not terminate after " + terminationTimeout);
        } catch (InterruptedException e) {
            log.warning("Interrupted while terminating thread pool: " + e.getMessage());
            // Preserve interrupt status.
            Thread.currentThread().interrupt();
        }
    }

    /** Receive a message from the group, blocking if necessary until one arrives. */
    public T receive() throws InterruptedException {
        Message<T> msg = deliver.take();
        return msg.payload;
    }

    /** Receive a message from the group, or return null if none are available. */
    public T tryReceive() {
        Message<T> msg = deliver.poll();
        if (msg == null)
            return null;
        return msg.payload;
    }

    /**
     * Receive a message from the group, waiting up to the specified wait time if necessary.
     *
     * @return A message received from the group, or null if the specified wait time elapsed
     * before a message arrived.
     */
    public T tryReceive(Duration waitTime) throws InterruptedException {
        Message<T> msg = deliver.poll(waitTime.toMillis(), TimeUnit.MILLISECONDS);
        if (msg == null)
            return null;
        return msg.payload;
    }
}
