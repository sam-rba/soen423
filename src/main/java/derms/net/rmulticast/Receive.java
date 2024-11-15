package derms.net.rmulticast;

import derms.net.ConcurrentMulticastSocket;
import derms.net.Packet;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

class Receive<T extends Serializable & Hashable> implements Runnable {
    private static final int bufSize = 8192;

    private final ConcurrentMulticastSocket inSock;
    private final Set<MessageID> acks;
    private final Set<MessageID> nacks; // Positively acknowledged messages.
    private final ReceivedSet<T> received; // Negatively acknowledged messages.
    private final BlockingQueue<Message<T>> retransmissions;
    private final Set<InetAddress> groupMembers;
    private final BlockingQueue<Message<T>> delivered;
    private final Logger log;

    Receive(ConcurrentMulticastSocket inSock, Set<MessageID> acks, Set<MessageID> nacks, ReceivedSet<T> received, BlockingQueue<Message<T>> retransmissions, Set<InetAddress> groupMembers, BlockingQueue<Message<T>> delivered) {
        this.inSock = inSock;
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
        DatagramPacket pkt = new DatagramPacket(new byte[bufSize], bufSize);
        Message<T> msg;
        for (;;) {
            try {
                inSock.receive(pkt);
                msg = Packet.decode(pkt, Message.class);
                receive(msg);
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private void receive(Message<T> msg) {
        groupMembers.add(msg.sender);

        if (msg instanceof AnnounceMessage)
            return;

        acks.add(msg.id());
        received.add(msg);
        delivered.add(msg);

        nacks.remove(msg.id());
        retransmissions.remove(msg);

        for (MessageID mid : msg.acks) {
            acks.remove(mid);
            if (!received.contains(mid))
                nacks.add(mid);
        }

        for (MessageID mid : msg.nacks) {
            if (received.contains(mid)) {
                retransmissions.add(msg);
            } else {
                nacks.add(mid);
            }
        }
    }
}
