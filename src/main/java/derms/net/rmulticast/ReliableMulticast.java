package derms.net.rmulticast;

import derms.net.ConcurrentMulticastSocket;
import derms.net.MessagePayload;
import derms.net.Packet;
import derms.util.ThreadPool;

import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * A reliable multicast protocol that guarantees delivery of messages in the event of a fail-stop.
 *
 * An implementation of the Trans protocol over IP multicast. Melliar-Smith, et al. "Broadcast Protocols for
 * Distributed Systems" in IEEE Transactions on Parallel and Distributed Systems, vol. 1, no. 1, 1990.
 */
public class ReliableMulticast<T extends MessagePayload> {
    private final SocketAddress group;
    private final InetAddress laddr; // Local address.
    private final Set<MessageID> acks; // Positively acknowledged messages.
    private final Set<MessageID> nacks; // Negatively acknowledged messages.
    private final ReceivedSet<T> received;
    private final BlockingQueue<Message<T>> retransmissions; // Messages pending retransmission.
    private final Set<InetAddress> groupMembers;
    private final ConcurrentMulticastSocket outSock;
    private final BlockingQueue<Message<T>> delivered;
    private final Logger log;
    private final ExecutorService pool;

    /**
     * Join the specified multicast group.
     *
     * @param group The IP address and port of the multicast group.
     * @param laddr The IP address of the local process.
     */
    public ReliableMulticast(InetSocketAddress group, InetAddress laddr) throws IOException {
        this.group = group;
        this.laddr = laddr;

        this.acks = ConcurrentHashMap.newKeySet();
        this.nacks = ConcurrentHashMap.newKeySet();
        this.received = new ReceivedSet<T>();
        this.retransmissions = new LinkedBlockingQueue<Message<T>>();
        this.groupMembers = ConcurrentHashMap.newKeySet();

        this.outSock = new ConcurrentMulticastSocket();
        this.outSock.joinGroup(group.getAddress());

        this.delivered = new LinkedBlockingQueue<Message<T>>();

        this.log = Logger.getLogger(this.getClass().getName());

        this.pool = Executors.newCachedThreadPool();
        pool.execute(new Receive<T>(group, acks, nacks, received, retransmissions, groupMembers, delivered));
        pool.execute(new Retransmit<T>(retransmissions, outSock, group));
        pool.execute(new Prune<T>(received, groupMembers));
        pool.execute(new Heartbeat(group, laddr, acks, nacks, outSock));
    }

    public void close() {
        log.info("Shutting down...");
        ThreadPool.shutDown(pool, log);
        outSock.close();
        log.info("Finished shutting down.");
    }

    /** Send a message to the group. */
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
        log.info("Sent " + msg + " from " + outSock + " to " + group);
    }

    /** Receive a message from the group, blocking if necessary until a message arrives. */
    public T receive() throws InterruptedException {
        Message<T> msg = delivered.take(); // Blocks until a message becomes available.
        return msg.payload;
    }

    /** Receive a message, or return null if none are available. */
    public T tryReceive() {
        Message<T> msg = delivered.poll();
        if (msg == null)
            return null;
        return msg.payload;
    }

    /**
     * Receive a message, waiting up to the specified wait time if necessary.
     *
     * @return A message received from the group, or null if the specified waiting
     * time elapses before a message arrives.
     */
    public T tryReceive(Duration waitTime) throws InterruptedException {
        Message<T> msg = delivered.poll(waitTime.toMillis(), TimeUnit.MILLISECONDS);
        if (msg == null)
            return null;
        return msg.payload;
    }
}
