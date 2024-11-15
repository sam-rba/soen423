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
    private final Set<MessageID> positiveAcks;
    private final Set<MessageID> negativeAcks;
    private final ReceivedSet<T> received;
    private final BlockingQueue<Message<T>> retransmissions;
    private final Set<InetAddress> groupMembers;
    private final BlockingQueue<Message<T>> delivered;
    private final Logger log;

    Receive(ConcurrentMulticastSocket inSock, Set<MessageID> positiveAcks, Set<MessageID> negativeAcks, BlockingQueue<Message<T>> retransmissions, Set<InetAddress> groupMembers, BlockingQueue<Message<T>> delivered) {
        this.inSock = inSock;
        this.positiveAcks = positiveAcks;
        this.negativeAcks = negativeAcks;
        this.received = new ReceivedSet<T>();
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
        positiveAcks.add(msg.id());
        received.add(msg);
        delivered.add(msg);

        groupMembers.add(msg.sender);

        negativeAcks.remove(msg.id());
        retransmissions.remove(msg);

        for (MessageID mid : msg.positiveAcks) {
            positiveAcks.remove(mid);
            if (!received.contains(mid))
                negativeAcks.add(mid);
        }

        for (MessageID mid : msg.negativeAcks) {
            if (received.contains(mid)) {
                retransmissions.add(msg);
            } else {
                negativeAcks.add(mid);
            }
        }
    }
}
