package derms.net.runicast;

import derms.net.ConcurrentDatagramSocket;
import derms.net.MessagePayload;
import derms.net.Packet;
import derms.util.ThreadPool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class ReliableUnicastSender<T extends MessagePayload> {
    private static final Duration soTimeout = Duration.ofMillis(500); // Socket timeout.

    private final AtomicLong next; // Next sequence number.
    private final AtomicLong unacked; // Sequence number of first unacknowledged message.
    private final Queue<Message<T>> sent;
    private final ConcurrentDatagramSocket sock;
    private final Logger log;
    private final ExecutorService pool;

    /**
     * @param raddr Remote IP address to connect to.
     */
    ReliableUnicastSender(InetSocketAddress raddr) throws IOException {
        this.next = new AtomicLong(0);
        this.unacked = new AtomicLong(0);
        this.sent = new LinkedBlockingQueue<Message<T>>();
        this.sock = new ConcurrentDatagramSocket();
        this.sock.connect(raddr);
        this.sock.setSoTimeout(soTimeout);
        this.log = Logger.getLogger(getClass().getName());
        this.pool = Executors.newCachedThreadPool();
        pool.execute(new ReceiveAcks<T>(unacked, sent, sock));
        pool.execute(new Retransmit<T>(unacked, sent, sock));
    }

    public void send(T payload) throws IOException {
        Message<T> msg = new Message<T>(next.get(), payload);
        DatagramPacket pkt = Packet.encode(msg, sock.getRemoteSocketAddress());
        sock.send(pkt);
        sent.add(msg);
        next.incrementAndGet();
    }

    /** Wait for all messages to be acknowledged and close the connection. */
    public void close() throws InterruptedException {
        // Wait for receiver to acknowledge all sent messages.
        while (unacked.get() < next.get()) {
            Thread.yield();
            if (Thread.interrupted())
                throw new InterruptedException();
        }

        closeNow();
    }

    /** Close the connection immediately, without waiting for acknowledgements. */
    public void closeNow() {
        log.info("Shutting down.");
        ThreadPool.shutDown(pool, log);
        sock.close();
    }
}
