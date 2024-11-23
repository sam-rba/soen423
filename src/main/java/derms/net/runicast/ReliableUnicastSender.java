package derms.net.runicast;

import derms.io.Serial;
import derms.net.MessagePayload;
import derms.util.ThreadPool;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/** The sending end of a reliable unicast connection. */
public class ReliableUnicastSender<T extends MessagePayload> {
    private final AtomicLong next; // Next sequence number.
    private final AtomicLong unacked; // Sequence number of first unacknowledged message.
    private final Queue<Message<T>> sent;
    private final DatagramChannel sock;
    private final Logger log;
    private final ExecutorService pool;

    /**
     * Connect to the specified address and port.
     *
     * @param raddr The remote IP address to connect to.
     */
    public ReliableUnicastSender(SocketAddress raddr) throws IOException {
        this.next = new AtomicLong(0);
        this.unacked = new AtomicLong(0);
        this.sent = new LinkedBlockingQueue<Message<T>>();
        this.sock = DatagramChannel.open();
        sock.connect(raddr);
        this.log = Logger.getLogger(getClass().getName());
        this.pool = Executors.newCachedThreadPool();
        pool.execute(new ReceiveAcks<T>(unacked, sent, sock));
        pool.execute(new Retransmit<T>(unacked, sent, sock));
    }

    public void send(T payload) throws IOException {
        Message<T> msg = new Message<T>(next.get(), payload);
        ByteBuffer buf = Serial.encode(msg);
        sock.send(buf, sock.getRemoteAddress());
        sent.add(msg);
        next.incrementAndGet();
        log.info("Sent " + msg);
    }

    /** Wait for all messages to be acknowledged and close the connection. */
    public void close() throws InterruptedException, IOException {
        // Wait for receiver to acknowledge all sent messages.
        log.info("Waiting for acknowledgements...");
        while (unacked.get() < next.get()) {
            Thread.yield();
            if (Thread.interrupted())
                throw new InterruptedException();
        }

        log.info("Shutting down.");
        sock.close();
        ThreadPool.shutdown(pool, log);
    }

    /** Close the connection immediately, without waiting for acknowledgements. */
    public void closeNow() throws IOException {
        log.info("Shutting down.");
        sock.close();
        ThreadPool.shutdownNow(pool, log);
    }
}
