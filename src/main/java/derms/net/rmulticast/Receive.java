package derms.net.rmulticast;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import derms.io.Serial;
import derms.net.MessagePayload;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

class Receive<T extends MessagePayload> implements Runnable {
    private static final int bufSize = 8192;

    private final DatagramChannel sock;
    private final Set<MessageID> acks; // Positively acknowledged messages.
    private final Set<MessageID> nacks; // Negatively acknowledged messages.
    private final ReceivedSet<T> received;
    private final BlockingQueue<Message<T>> retransmissions;
    private final Set<InetAddress> groupMembers;
    private final BlockingQueue<Message<T>> delivered;
    private final Logger log;

    Receive(DatagramChannel sock, Set<MessageID> acks, Set<MessageID> nacks, ReceivedSet<T> received, BlockingQueue<Message<T>> retransmissions, Set<InetAddress> groupMembers, BlockingQueue<Message<T>> delivered) throws IOException {
        this.acks = acks;
        this.nacks = nacks;
        this.received = received;
        this.retransmissions = retransmissions;
        this.groupMembers = groupMembers;
        this.sock = sock;
        this.delivered = delivered;
        this.log = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void run() {
        try {
            log.info("Listening on " + sock.getLocalAddress());
        } catch (IOException e) {
            log.warning(e.getMessage());
        }

        ByteBuffer buf = ByteBuffer.allocate(bufSize);
        for (;;) {
            buf.clear();
            try {
                sock.receive(buf);
                Message<?> msg = Serial.decode(buf, Message.class);
                receive(msg);
                log.info("Received " + msg);
            } catch (ClosedChannelException e) {
                log.info("Shutting down.");
                return;
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
        retransmissions.remove(msg);
        if (received.add(msg))
            delivered.add(msg); // First time seeing this message; deliver it.
    }
}
