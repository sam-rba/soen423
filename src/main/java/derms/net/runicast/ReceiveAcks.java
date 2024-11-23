package derms.net.runicast;

import derms.io.Serial;
import derms.net.MessagePayload;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/** Receive acknowledgements. Remove messages from the sent queue once they are acknowledged. */
class ReceiveAcks<T extends MessagePayload> implements Runnable {
    private static final int bufSize = 8192;

    private final AtomicLong unacked;
    private final Queue<Message<T>> sent;
    private final DatagramChannel sock;
    private final Logger log;

    ReceiveAcks(AtomicLong unacked, Queue<Message<T>> sent, DatagramChannel sock) {
        this.unacked = unacked;
        this.sent = sent;
        this.sock = sock;
        this.log = Logger.getLogger(getClass().getName());
    }

    @Override
    public void run() {
        ByteBuffer buf = ByteBuffer.allocate(bufSize);
        for (;;) {
            buf.clear();
            try {
                sock.receive(buf);
                Ack ack = Serial.decode(buf, Ack.class);
                recvAck(ack.seq);
            } catch (ClosedChannelException e) {
                log.info("Shutting down.");
                return;
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private void recvAck(long ack) {
        log.info("Received ack: " + ack);
        unacked.updateAndGet((unacked) -> {
            if (ack >= unacked)
                return ack+1;
            return unacked;
        });

        while (!sent.isEmpty() && sent.peek().seq <= ack)
            sent.remove();
    }
}
