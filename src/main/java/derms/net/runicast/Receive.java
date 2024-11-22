package derms.net.runicast;

import derms.net.ConcurrentDatagramSocket;
import derms.net.MessagePayload;
import derms.net.Packet;

import java.io.IOException;
import java.net.*;
import java.util.Queue;
import java.util.logging.Logger;

class Receive<T extends MessagePayload> implements Runnable {
    private static final int bufSize = 8192;

    private long seq; // Sequence number.
    private final ConcurrentDatagramSocket sock;
    private final Queue<T> delivered;
    private final Logger log;

    Receive(ConcurrentDatagramSocket sock, Queue<T> delivered) {
        this.seq = 0;
        this.sock = sock;
        this.delivered = delivered;
        this.log = Logger.getLogger(getClass().getName());
    }

    @Override
    public void run() {
        DatagramPacket pkt = new DatagramPacket(new byte[bufSize], bufSize);
        for (;;) {
            try {
                sock.receive(pkt);
                Message<T> msg = (Message<T>) Packet.decode(pkt, Message.class);
                SocketAddress sender = pkt.getSocketAddress();
                recv(msg, sender);
            } catch (SocketTimeoutException e) {
                if (Thread.interrupted()) {
                    log.info("Interrupted");
                    return;
                }
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                log.warning(e.getMessage());
            }
        }
    }

    private void recv(Message<T> msg, SocketAddress sender) throws IOException {
        if (msg.seq == seq) {
            delivered.add(msg.payload);
            ack(msg, sender);
            seq++;
        }
    }

    private void ack(Message<T> msg, SocketAddress sender) throws IOException {
        Ack ack = new Ack(msg.seq);
        DatagramPacket pkt = Packet.encode(ack, sender);
        sock.send(pkt);
    }
}
