package derms.net.runicast;

import derms.io.Serial;
import derms.net.MessagePayload;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.util.Queue;
import java.util.logging.Logger;

class Receive<T extends MessagePayload> implements Runnable {
    private static final int bufSize = 8192;

    private long seq; // Sequence number.
    private final DatagramChannel sock;
    private final Queue<T> delivered;
    private final Logger log;

    Receive(DatagramChannel sock, Queue<T> delivered) {
        this.seq = 0;
        this.sock = sock;
        this.delivered = delivered;
        this.log = Logger.getLogger(getClass().getName());
    }

    @Override
    public void run() {
        ByteBuffer buf = ByteBuffer.allocate(bufSize);
        for (;;) {
            buf.clear();
            try {
                SocketAddress sender = sock.receive(buf);
                Message<T> msg = (Message<T>) Serial.decode(buf, Message.class);
                recv(msg, sender);
            } catch (ClosedChannelException e) {
                log.info("Shutting down.");
                return;
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private void recv(Message<T> msg, SocketAddress sender) throws IOException {
        log.info("Received " + msg);
        if (msg.seq == seq) {
            delivered.add(msg.payload);
            log.info("Delivered " + msg);
            ack(msg, sender);
            log.info("Acked " + msg);
            seq++;
        }
    }

    private void ack(Message<T> msg, SocketAddress sender) throws IOException {
        Ack ack = new Ack(msg.seq);
        ByteBuffer buf = Serial.encode(ack);
        sock.send(buf, sender);
    }
}
