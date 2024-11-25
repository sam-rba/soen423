package derms.net.rmulticast;

import derms.io.Serial;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.time.Duration;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Periodically send acknowledgements to the group. Allows acks to propagate even when some
 * processes wouldn't normally send messages. Also allows processes to populate their set of
 * group members.
 */
class Heartbeat implements Runnable {
    private static final Duration period = Duration.ofSeconds(30);

    private final InetSocketAddress group;
    private final InetAddress laddr;
    private final Set<MessageID> acks, nacks;
    private final DatagramChannel sock;
    private final Logger log;

    Heartbeat(InetSocketAddress group, InetAddress laddr, Set<MessageID> acks, Set<MessageID> nacks, DatagramChannel sock) throws IOException {
        this.group = group;
        this.laddr = laddr;
        this.acks = acks;
        this.nacks = nacks;
        this.sock = sock;
        this.log = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void run() {
        for (;;) {
            try {
                Thread.sleep(period.toMillis());
                send();
            } catch (InterruptedException | ClosedChannelException e) {
                log.info("Shutting down.");
                return;
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private void send() throws IOException {
        HeartbeatMessage msg = new HeartbeatMessage(
                laddr,
                acks.toArray(new MessageID[0]),
                nacks.toArray(new MessageID[0]));
        ByteBuffer buf = Serial.encode(msg);
        sock.send(buf, group);
        acks.clear();
    }
}
