package derms.net.il;

import derms.net.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Connection implements Runnable {
    private static final Random rng = new Random();
    private static final Duration terminationTimeout = Duration.ofSeconds(1);

    AtomicReference<State> state;
    final InetAddress laddr; // Local IP address.
    final int lport; // Local IL port.
    final InetAddress raddr; // Remote IP address.
    final int rport; // Remote IL port.
    final int id0; // Starting sequence number of the local side.
    final int rid0; // Starting sequence number of the remote side.
    int next; // Sequence number of the next message to be sent from the local side.
    int rcvd; // The last in-sequence message received from the remote side.
    int unacked; // Sequence number of the first unacked message.
    DatagramSocket sock;
    private final Logger log;
    private final ExecutorService pool;

    Connection(InetAddress laddr, int lport, InetAddress raddr, int rport, DatagramSocket sock) {
        this.state = new AtomicReference<State>(State.closed);
        this.laddr = laddr;
        this.lport = lport;
        this.raddr = raddr;
        this.rport = rport;
        this.id0 = rng.nextInt();
        this.next = this.id0;
        this.unacked = this.id0;
        this.sock = sock;
        this.log = Logger.getLogger(this.getClass().getName());
        this.pool = Executors.newCachedThreadPool();
    }

    public void close() {
        state.set(State.closed);

        pool.shutdownNow();
        try {
            if (!pool.awaitTermination(terminationTimeout.toMillis(), TimeUnit.MILLISECONDS))
                log.warning("Thread pool did not terminate after " + terminationTimeout);
        } catch (InterruptedException e) {
            log.warning("Interrupted while terminating thread pool: " + e.getMessage());
            // Preserve interrupt status.
            Thread.currentThread().interrupt();
        }

        sock.close();
        log.info("Finished shutting down.");
    }

    void start() {
        pool.execute(this);
    }

    void sendCtl(Type type, int id, int ack) throws IOException {
        ControlMessage msg = new ControlMessage(type, id, ack);
        DatagramPacket pkt = Packet.encode(msg, raddr, rport);
        sock.send(pkt);
    }

    @Override
    public void run() {
        for (;;) {
            if (Thread.interrupted())
                return;

            switch (state.get()) {
                case closed:
                    return;

                case syncer:
                    // TODO
                    continue;

                case syncee:
                    // TODO
                    continue;

                case established:
                    // TODO
                    continue;

                case listening:
                    // TODO
                    continue;

                case closing:
                    // TODO
                    continue;

                case opening:
                    // TODO
                    continue;

                default:
                    throw new IllegalStateException("illegal connection state: " + state);
            }
        }
    }
}
