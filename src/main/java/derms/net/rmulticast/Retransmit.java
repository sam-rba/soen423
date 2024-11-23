package derms.net.rmulticast;

import derms.io.Serial;
import derms.net.MessagePayload;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/** Retransmit dropped messages. */
class Retransmit<T extends MessagePayload> implements Runnable {
    private final BlockingQueue<Message<T>> retransmissions;
    private final DatagramChannel sock;
    private final SocketAddress group;
    private final Logger log;

    Retransmit(BlockingQueue<Message<T>> retransmissions, DatagramChannel sock, SocketAddress group) {
        this.retransmissions = retransmissions;
        this.sock = sock;
        this.group = group;
        this.log = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void run() {
        for (;;) {
            try {
                Message<T> msg = retransmissions.take();
                ByteBuffer buf = Serial.encode(msg);
                sock.send(buf, group);
                log.info("Retransmitted " + msg);
            } catch (InterruptedException | ClosedChannelException e) {
                log.info("Shutting down.");
                return;
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
        }
    }
}
