package derms.net.runicast;

import derms.io.Serial;
import derms.net.MessagePayload;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/** Retransmit unacknowledged messages. */
class Retransmit<T extends MessagePayload> implements Runnable {
    private static final Duration period = Duration.ofMillis(500);

    private final AtomicLong unacked;
    private final Queue<Message<T>> sent;
    private final DatagramChannel sock;
    private final Logger log;

    Retransmit(AtomicLong unacked, Queue<Message<T>> sent, DatagramChannel sock) {
        this.unacked = unacked;
        this.sent = sent;
        this.sock = sock;
        this.log = Logger.getLogger(getClass().getName());
    }

    @Override
    public void run() {
        for (;;) {
            try {
                Thread.sleep(period.toMillis());

                for (Message<T> msg : sent) {
                    if (msg.seq >= unacked.get()) {
                        retransmit(msg);
                    }
                }
            } catch (InterruptedException | ClosedChannelException e) {
                log.info("Shutting down.");
                return;
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private void retransmit(Message<T> msg) throws IOException {
        ByteBuffer buf = Serial.encode(msg);
        sock.send(buf, sock.getRemoteAddress());
        log.info("Retransmitted " + msg);
    }
}
