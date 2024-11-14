package derms.net.rmulticast;

import derms.net.ConcurrentMulticastSocket;
import derms.net.Packet;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/** Retransmit dropped messages. */
class Retransmit<T extends Serializable & Hashable> implements Runnable {
    private final BlockingQueue<Message<T>> retransmissions;
    private final ConcurrentMulticastSocket outSock;
    private final SocketAddress group;
    private final Logger log;

    Retransmit(BlockingQueue<Message<T>> retransmissions, ConcurrentMulticastSocket outSock, SocketAddress group) {
        this.retransmissions = retransmissions;
        this.outSock = outSock;
        this.group = group;
        this.log = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                Message<T> msg = retransmissions.take();
                try {
                    DatagramPacket pkt = Packet.encode(msg, group);
                    outSock.send(pkt);
                } catch (Exception e) {
                    log.warning(e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            log.info("Retransmit thread interrupted: " + e.getMessage());
        }
    }
}
