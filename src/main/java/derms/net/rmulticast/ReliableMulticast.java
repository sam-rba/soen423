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
    private final Set<MessageID> positiveAcks; // Positively acknowledged messages.
    private final Set<MessageID> negativeAcks; // Negatively acknowledged messages.
    private final ReceivedSet<T> received;
    private final BlockingQueue<Message<T>> retransmissions; // Messages pending retransmission.
    private final AtomicReference<Instant> lastSend;
    private final SocketAddress group;
    private final ConcurrentMulticastSocket outSock;
    private final InetAddress laddr; // Local address.
    private final BlockingQueue<Message<T>> delivered;
    private final Logger log;

    public ReliableMulticast(InetSocketAddress group, InetAddress laddr) throws IOException {
        this.positiveAcks = new ConcurrentHashMap<MessageID, Void>().keySet();
        this.negativeAcks = new ConcurrentHashMap<MessageID, Void>().keySet();
        this.received = new ReceivedSet<T>();
        this.retransmissions = new LinkedBlockingQueue<Message<T>>();
        this.lastSend = new AtomicReference<Instant>(Instant.now());

        this.group = group;

        this.outSock = new ConcurrentMulticastSocket(group.getPort());
        this.outSock.joinGroup(group.getAddress());

        this.laddr = laddr;

        this.delivered = new LinkedBlockingQueue<Message<T>>();

        this.log = Logger.getLogger(this.getClass().getName());

        ConcurrentMulticastSocket inSock = new ConcurrentMulticastSocket();
        inSock.joinGroup(group.getAddress());
        (new Thread(new Receive<T>(inSock, positiveAcks, negativeAcks, received, retransmissions, delivered))).start();

        (new Thread(new Retransmit<T>(retransmissions, outSock, group))).start();
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
        (new Thread(new Timeout<T>(msg, positiveAcks, retransmissions))).start();
        lastSend.set(Instant.now());
    }

    public T receive() throws InterruptedException {
        Message<T> msg = delivered.take();
        return msg.payload;
    }
}
