package derms.net.rmulticast;

import derms.net.ConcurrentMulticastSocket;
import derms.net.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

class Receive<T extends MessagePayload> implements Runnable {
    private static final int bufSize = 8192;
    private static final Duration sockTimeout = Duration.ofMillis(500);

    private final ConcurrentMulticastSocket inSock;
    private final Set<MessageID> acks;
    private final Set<MessageID> nacks; // Positively acknowledged messages.
    private final ReceivedSet<T> received; // Negatively acknowledged messages.
    private final BlockingQueue<Message<T>> retransmissions;
    private final Set<InetAddress> groupMembers;
    private final BlockingQueue<Message<T>> delivered;
    private final Logger log;

    Receive(InetSocketAddress group, Set<MessageID> acks, Set<MessageID> nacks, ReceivedSet<T> received, BlockingQueue<Message<T>> retransmissions, Set<InetAddress> groupMembers, BlockingQueue<Message<T>> delivered) throws IOException {
        this.inSock = new ConcurrentMulticastSocket(group.getPort());
        this.inSock.joinGroup(group.getAddress());
        this.inSock.setSoTimeout(sockTimeout);

        this.acks = acks;
        this.nacks = nacks;
        this.received = received;
        this.retransmissions = retransmissions;
        this.groupMembers = groupMembers;
        this.delivered = delivered;
        this.log = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void run() {
        log.info("Listening on " + inSock);
        DatagramPacket pkt = new DatagramPacket(new byte[bufSize], bufSize);
        for (;;) {
            try {
                inSock.receive(pkt);
                Message<?> msg = Packet.decode(pkt, Message.class);
                receive(msg);
                log.info("Received " + msg);
            } catch (SocketTimeoutException e) {
                if (Thread.interrupted()) {
                    log.info("Interrupted. Shutting down.");
                    inSock.close();
                    return;
                }
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private void receive(Message<?> msg) throws ClassCastException {
        groupMembers.add(msg.sender);
        receiveAcks(msg);
        receiveNacks(msg);

        if (msg instanceof HeartbeatMessage)
            return; // Only deliver in-band messages.
        deliver((Message<T>) msg);
    }

    private void receiveAcks(Message<?> msg) {
        for (MessageID mid : msg.acks) {
            acks.remove(mid);
            if (!received.contains(mid))
                nacks.add(mid);
        }
    }

    private void receiveNacks(Message<?> msg) {
        for (MessageID mid : msg.nacks) {
            try {
                Message<T> rmsg = received.getByID(mid);
                retransmissions.add(rmsg);
            } catch (NoSuchElementException e) {
                nacks.add(mid);
            }
        }
    }

    private void deliver(Message<T> msg) {
        acks.add(msg.id());
        nacks.remove(msg.id());
        received.add(msg);
        retransmissions.remove(msg);
        delivered.add(msg);
    }
}
