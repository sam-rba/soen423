package derms.net.rmulticast;

import derms.net.ConcurrentMulticastSocket;
import derms.net.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * Periodically announce the presence of the local process to the group.
 * Allows processes to populate their set of group members, even if not
 * all members would normally send messages to the group.
 */
class Announce implements Runnable {
    private static final Duration period = Duration.ofSeconds(30);

    private final DatagramPacket pkt;
    private final ConcurrentMulticastSocket outSock;
    private final Logger log;

    Announce(InetSocketAddress group, InetAddress laddr, ConcurrentMulticastSocket outSock) throws IOException {
        AnnounceMessage msg = new AnnounceMessage(laddr);
        this.pkt = Packet.encode(msg, group);
        this.outSock = outSock;
        this.log = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void run() {
        try {
            for (;;) {
                try {
                    Wait.forDuration(period);
                    outSock.send(pkt);
                } catch (IOException e) {
                    log.warning(e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            log.info("Announce thread interrupted: " + e.getMessage());
        }
    }
}
