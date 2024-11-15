package derms.net.rmulticast;

import derms.net.ConcurrentMulticastSocket;
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
    private final SocketAddress group;
    private final InetAddress laddr; // Local address.
    private final Set<MessageID> acks; // Positively acknowledged messages.
    private final Set<MessageID> nacks; // Negatively acknowledged messages.
    private final ReceivedSet<T> received;
    private final BlockingQueue<Message<T>> retransmissions; // Messages pending retransmission.
    private final Set<InetAddress> groupMembers;
    private final ConcurrentMulticastSocket outSock;
    private final AtomicReference<Instant> lastSend;
    private final BlockingQueue<Message<T>> delivered;
    private final Logger log;

    public ReliableMulticast(InetSocketAddress group, InetAddress laddr) throws IOException {
        this.group = group;
        this.laddr = laddr;

        this.acks = new ConcurrentHashMap<MessageID, Void>().keySet();
        this.nacks = new ConcurrentHashMap<MessageID, Void>().keySet();
        this.received = new ReceivedSet<T>();
        this.retransmissions = new LinkedBlockingQueue<Message<T>>();
        this.groupMembers = new ConcurrentHashMap<InetAddress, Void>().keySet();
        this.lastSend = new AtomicReference<Instant>(Instant.now());

        this.outSock = new ConcurrentMulticastSocket(group.getPort());
        this.outSock.joinGroup(group.getAddress());

        this.delivered = new LinkedBlockingQueue<Message<T>>();

        this.log = Logger.getLogger(this.getClass().getName());

        ConcurrentMulticastSocket inSock = new ConcurrentMulticastSocket();
        inSock.joinGroup(group.getAddress());
        (new Thread(new Receive<T>(inSock, acks, nacks, received, retransmissions, groupMembers, delivered))).start();

        (new Thread(new Retransmit<T>(retransmissions, outSock, group))).start();

        (new Thread(new Prune<T>(received, groupMembers))).start();

        try {
            (new Thread(new Announce(group, laddr, outSock))).start();
        } catch (IOException e) {
            log.severe("Failed to start announce thread: " + e.getMessage());
            throw e;
        }
    }

    public void send(T payload) throws IOException {
        Message<T> msg = new Message<T>(
                payload,
                laddr,
                acks.toArray(new MessageID[0]),
                nacks.toArray(new MessageID[0]));
        DatagramPacket pkt = Packet.encode(msg, group);
        outSock.send(pkt);
        acks.clear();
        (new Thread(new Timeout<T>(msg, acks, retransmissions))).start();
        lastSend.set(Instant.now());
    }

    public T receive() throws InterruptedException {
        Message<T> msg = delivered.take();
        return msg.payload;
    }
}
