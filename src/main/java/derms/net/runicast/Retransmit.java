package derms.net.runicast;

import derms.net.ConcurrentDatagramSocket;
import derms.net.MessagePayload;
import derms.net.Packet;
import derms.util.Wait;

import java.io.IOException;
import java.net.DatagramPacket;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/** Retransmit unacknowledged messages. */
class Retransmit<T extends MessagePayload> implements Runnable {
    private static final Duration timeout = Duration.ofMillis(500);

    private final AtomicLong unacked;
    private final Queue<Message<T>> sent;
    private final ConcurrentDatagramSocket sock;
    private final Logger log;

    Retransmit(AtomicLong unacked, Queue<Message<T>> sent, ConcurrentDatagramSocket sock) {
        this.unacked = unacked;
        this.sent = sent;
        this.sock = sock;
        this.log = Logger.getLogger(getClass().getName());
    }

    @Override
    public void run() {
        try {
            for (;;) {
                Wait.forDuration(timeout);

                for (Message<T> msg : sent) {
                    if (msg.seq >= unacked.get()) {
                        retransmit(msg);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.info("Interrupted.");
        }
    }

    private void retransmit(Message<T> msg) {
        try {
            DatagramPacket pkt = Packet.encode(msg, sock.getRemoteSocketAddress());
            sock.send(pkt);
            log.info("Retransmitted " + msg);
        } catch (IOException e) {
            log.warning("Failed to retransmit " + msg + ": " + e.getMessage());
        }
    }
}
