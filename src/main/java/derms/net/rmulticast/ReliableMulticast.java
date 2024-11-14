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
    private final Set<MessageID> retransmissions; // Messages pending retransmission.
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
        this.retransmissions = new ConcurrentHashMap<MessageID, Void>().keySet();
        this.lastSend = new AtomicReference<Instant>(Instant.now());

        this.group = group;

        this.inSock = new MulticastSocket();
        this.inSock.joinGroup(group.getAddress());

        this.outSock = new MulticastSocket(group.getPort());
        this.outSock.joinGroup(group.getAddress());

        this.laddr = laddr;

        this.delivered = new LinkedBlockingQueue<Message<T>>();
        (new Thread(new Receiver())).start();

        this.log = Logger.getLogger(this.getClass().getName());
    }

    public void send(T payload) throws IOException {
        Message<T> msg = new Message<T>(
                payload,
                laddr,
                positiveAcks.toArray(new MessageID[0]),
                negativeAcks.toArray(new MessageID[0]));
        DatagramPacket pkt = Packet.encode(msg, group);
        outSock.send(pkt);
        positiveAcks.clear();
        (new Thread(new Timeout(msg.id()))).start();
        lastSend.set(Instant.now());
    }

    private class Receiver implements Runnable {
        @Override
        public void run() {
            // TODO
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
