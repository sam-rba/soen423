package derms.net.runicast;

import derms.net.ConcurrentDatagramSocket;
import derms.net.MessagePayload;
import derms.net.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/** Receive acknowledgements. Remove messages from the sent queue once they are acknowledged. */
class ReceiveAcks<T extends MessagePayload> implements Runnable {
    private static final int bufSize = 8192;

    private final AtomicLong unacked;
    private final Queue<Message<T>> sent;
    private final ConcurrentDatagramSocket sock;
    private final Logger log;

    ReceiveAcks(AtomicLong unacked, Queue<Message<T>> sent, ConcurrentDatagramSocket sock) {
        this.unacked = unacked;
        this.sent = sent;
        this.sock = sock;
        this.log = Logger.getLogger(getClass().getName());
    }

    @Override
    public void run() {
        DatagramPacket pkt = new DatagramPacket(new byte[bufSize], bufSize);
        for (;;) {
            try {
                sock.receive(pkt);
                Ack ack = Packet.decode(pkt, Ack.class);
                recvAck(ack.seq);
            } catch (SocketTimeoutException e) {
                if (Thread.interrupted()) {
                    log.info("Interrupted.");
                    return;
                }
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private void recvAck(long ack) {
        unacked.updateAndGet((unacked) -> {
            if (ack >= unacked)
                return ack+1;
            return unacked;
        });

        while (!sent.isEmpty() && sent.peek().seq <= ack)
            sent.remove();
    }
}
