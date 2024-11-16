package derms.net.rmulticast;

import derms.net.ConcurrentMulticastSocket;
import derms.net.Packet;
import derms.util.Wait;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
    private final ConcurrentMulticastSocket outSock;
    private final Logger log;

    Heartbeat(InetSocketAddress group, InetAddress laddr, Set<MessageID> acks, Set<MessageID> nacks, ConcurrentMulticastSocket outSock) throws IOException {
        this.group = group;
        this.laddr = laddr;
        this.acks = acks;
        this.nacks = nacks;
        this.outSock = outSock;
        this.log = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void run() {
        try {
            for (;;) {
                try {
                    Wait.forDuration(period);
                    send();
                } catch (IOException e) {
                    log.warning(e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            log.info("Interrupted. Shutting down.");
        }
    }

    private void send() throws IOException {
        HeartbeatMessage msg = new HeartbeatMessage(
                laddr,
                acks.toArray(new MessageID[0]),
                nacks.toArray(new MessageID[0]));
        DatagramPacket pkt = Packet.encode(msg, group);
        outSock.send(pkt);
        acks.clear();
    }
}
