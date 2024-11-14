package derms.net.rmulticast;

import derms.net.Packet;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/** TODO */
public class ReliableMulticast<T extends Serializable & Hashable> {
    private final Set<MessageID> positiveAcks; // Positively acknowledged messages.
    private final Set<MessageID> negativeAcks; // Negatively acknowledged messages.
    private final Set<Message<T>> received;
    private final BlockingQueue<Message<T>> retransmissions; // Messages pending retransmission.
    private final AtomicReference<Instant> lastSend;
    private final SocketAddress group;
    private final MulticastSocket inSock, outSock;
    private final InetAddress laddr; // Local address.
    private final BlockingQueue<Message<T>> delivered;
    private final Logger log;

    public ReliableMulticast(InetSocketAddress group, InetAddress laddr) throws IOException {
        this.positiveAcks = new ConcurrentHashMap<MessageID, Void>().keySet();
        this.negativeAcks = new ConcurrentHashMap<MessageID, Void>().keySet();
        this.received = new ConcurrentHashMap<Message<T>, Void>().keySet();
        this.retransmissions = new LinkedBlockingQueue<Message<T>>();
        this.lastSend = new AtomicReference<Instant>(Instant.now());

        this.group = group;

        this.inSock = new MulticastSocket();
        this.inSock.joinGroup(group.getAddress());

        this.outSock = new MulticastSocket(group.getPort());
        this.outSock.joinGroup(group.getAddress());

        this.laddr = laddr;

        this.delivered = new LinkedBlockingQueue<Message<T>>();

        this.log = Logger.getLogger(this.getClass().getName());

        (new Thread(new Receive())).start();
        (new Thread(new Retransmit())).start();
    }

    public void send(T payload) throws IOException {
        Message<T> msg = new Message<T>(
                payload,
                laddr,
                positiveAcks.toArray(new MessageID[0]),
                negativeAcks.toArray(new MessageID[0]));
        DatagramPacket pkt = Packet.encode(msg, group);
        synchronized (outSock) {
            outSock.send(pkt);
        }
        positiveAcks.clear();
        (new Thread(new Timeout(msg.id()))).start();
        lastSend.set(Instant.now());
    }

    private class Receive implements Runnable {
        @Override
        public void run() {
            // TODO
        }
    }

    /** Retransmit dropped messages. */
    private class Retransmit implements Runnable {
        private final Logger log;

        private Retransmit() {
            this.log = Logger.getLogger(this.getClass().getName());
        }

        @Override
        public void run() {
            try {
                for (;;) {
                    Message<T> msg = retransmissions.take();
                    try {
                        DatagramPacket pkt = Packet.encode(msg, group);
                        synchronized (outSock) {
                            outSock.send(pkt);
                        }
                    } catch (Exception e) {
                        log.warning(e.getMessage());
                    }
                }
            } catch (InterruptedException e) {
                log.info("Retransmit thread interrupted: "+e.getMessage());
            }
        }
    }

    private class Timeout implements Runnable {
        MessageID mid;

        private Timeout(MessageID mid) {
            this.mid = mid;
        }

        @Override
        public void run() {
            // TODO
        }
    }
}
