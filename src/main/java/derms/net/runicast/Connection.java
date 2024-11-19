package derms.net.runicast;

import derms.net.ConcurrentDatagramSocket;
import derms.net.Packet;

import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Connection implements Runnable {
    private static final Random rng = new Random();
    private static final Duration roundTrip = Duration.ofMillis(100); // TODO: measure round trip time.
    private static final Duration rexitTimeout = roundTrip.multipliedBy(4);
    private static final Duration deathTimeout = roundTrip.multipliedBy(300);
    private static final int bufSize = 8192;
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
    ConcurrentDatagramSocket sock;
    private final Logger log;
    private final ExecutorService pool;

    Connection(InetAddress laddr, int lport, InetAddress raddr, int rport, ConcurrentDatagramSocket sock) {
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

            try {
                switch (state.get()) {
                    case closed:
                        return;
                    case syncer:
                        syncer();
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
            } catch (IOException | ClassNotFoundException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private void syncer() throws IOException, ClassNotFoundException {
        Instant tstart = Instant.now();
        for (;;) {
            try {
                sendCtl(Type.sync, id0, 0);

                ControlMessage msg = recvCtl(rexitTimeout);
                if (msg.type == Type.ack && msg.ack != id0
                        || msg.type == Type.close && msg.ack == id0) {
                    state.set(State.closed);
                    return;
                } else if (msg.type == Type.sync && msg.ack == id0) {
                    state.set(State.established);
                    return;
                }
            } catch (SocketTimeoutException e) {
                Duration elapsed = Duration.between(tstart, Instant.now());
                if (elapsed.compareTo(deathTimeout) > 0) {
                    state.set(State.closed);
                    return;
                }
            }
        }
    }

    private ControlMessage recvCtl(Duration timeout) throws IOException, SocketTimeoutException, ClassNotFoundException {
        DatagramPacket pkt = new DatagramPacket(new byte[bufSize], bufSize);
        sock.setSoTimeout(timeout);
        sock.receive(pkt);
        return Packet.decode(pkt, ControlMessage.class);
    }
}
