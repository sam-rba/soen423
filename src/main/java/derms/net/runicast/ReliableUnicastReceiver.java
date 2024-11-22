package derms.net.runicast;

import derms.net.ConcurrentDatagramSocket;
import derms.net.MessagePayload;
import derms.util.ThreadPool;

import java.io.IOException;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ReliableUnicastReceiver<T extends MessagePayload> {
    private static final Duration soTimeout = Duration.ofMillis(500); // Socket timeout.

    private final ConcurrentDatagramSocket sock;
    private final BlockingQueue<T> delivered;
    private final Logger log;
    private final ExecutorService pool;

    /**
     * @param laddr The local IP address and port to listen on.
     */
    ReliableUnicastReceiver(SocketAddress laddr) throws IOException {
        this.sock = new ConcurrentDatagramSocket(laddr);
        this.sock.setSoTimeout(soTimeout);
        this.delivered = new LinkedBlockingQueue<T>();
        this.log = Logger.getLogger(getClass().getName());
        this.pool = Executors.newCachedThreadPool();
        pool.execute(new Receive<T>(sock, delivered));
    }

    public void close() {
        log.info("Shutting down");
        ThreadPool.shutDown(pool, log);
        sock.close();
    }

    /** Receive a message, blocking if necessary until one arrives. */
    public T receive() throws InterruptedException {
        return delivered.take();
    }

    /** Receive a message, or return null if none are available. */
    public T tryReceive() {
        return delivered.poll();
    }

    /**
     * Receive a message, waiting up to the specified wait time if necessary.
     *
     * @return A message, or null if the specified waiting time elapses before
     * a message arrives.
     */
    public T tryReceive(Duration waitTime) throws InterruptedException {
        return delivered.poll(waitTime.toMillis(), TimeUnit.MILLISECONDS);
    }
}
